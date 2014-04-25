package cpup.cbot.test

import cpup.cbot.plugin.Plugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.users._
import cpup.cbot.events.channel.{ChannelMessageEvent, ChannelEvent}
import cpup.cbot.channels.Channel
import cpup.cbot.events.channel.ChannelMessageEvent

class UsersPlugin extends Plugin {
	@Subscribe
	def users(e: TCommandCheckEvent) {
		e.command(
			name = "users",
			usages = List(
				"whois [user]",
				"login <username> <password>",
				"logout",
				"setpass[word] <new password>",
				"register <nick> <username>",
				"register-nickserv"
			),
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					printUsage()
				} else {
					e.args(0) match {
						case "login" =>
							if(e.args.length < 2) {
								printUsage()
							} else {
								try {
									e.user.login(e.args(1), e.args(2))
									e.reply(s"Logged in as ${e.args(1)}")
								} catch {
									case ex: UnknownUserException =>
										e.reply(s"Unknown User: ${e.args(1)}")

									case ex: IncorrectPasswordException =>
										e.reply(s"Incorrect Password")
								}
							}

						case "logout" =>
							e.user.logout

						case "setpass" | "setpassword" =>
							if(e.args.length < 2) {
								printUsage()
							} else {
								e.user.user.password = User.hash(e.args(1))
							}

						case "whois" =>
							var user = e.user

							if(e.args.length >= 2) {
								user = e.bot.users.fromNick(e.args(1))
							}

							e.genericReply(s"--[===[${user.nick}]===]--")
							e.genericReply(s"Logged in as ${user.user.username}")

						case "register" =>
							if(e.args.length < 3) {
								printUsage()
							} else {
								if(!e.bot.checkPermission(e.user.user, 'register)) {
									e.reply("Insufficient Permission")
									return ()
								}

								try {
									val user = e.bot.users.fromNick(e.args(1))
									if(user.user.isInstanceOf[GuestUser]) {
										user.user = e.bot.users.register(e.args(2))
										e.genericReply(s"Created account: ${e.args(2)}")
										e.genericReply(s"${user.nick} logged into ${e.args(2)}")
									} else {
										e.reply(s"${user.nick} has already registered")
									}
								} catch {
									case ex: AlreadyRegisteredException =>
										e.reply("Username is already in use")
								}
							}

						case "register-nickserv" =>
							try {
								e.user.registerNickServ
								e.reply(s"Registered NickServ account: ${e.user.nickserv} to user: ${e.user.user.username}")
							} catch {
								case ex: GuestUserException =>
									e.reply("Cannot register NickServ account to the user because you aren't logged in")

								case ex: AlreadyRegisteredException =>
									e.reply("Your NickServ account is already registered to a user")
							}

						case _ =>
							printUsage()
					}

					()
				}
			}
		)
	}

	@Subscribe
	def perms(e: TCommandCheckEvent) {
		e.command(
			name = "perms",
			usages = List(
				"list [user]",
				"grant [context] [user] <permissions>",
				"take [context] [user] <permissions>"
			),
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					printUsage()
				} else {
					e.args(0) match {
						case "list" =>
							var context = e.context
							var user = e.user.user

							for(arg <- e.args.view(1, e.args.length)) {
								val argContext = e.bot.getContext(arg)
								if(argContext != null) {
									context = argContext
								} else {
									user = e.bot.users.fromNick(arg).user
								}
							}

							e.reply(s"${user.username}'s permissions in $context: ${context.getPermissions(user).mkString(", ")}")

						case "grant" =>
							var context = e.context
							var user = e.user.user
							val permissions = e.args(e.args.length - 1)

							for(arg <- e.args.view(1, e.args.length - 1)) {
								val argContext = e.bot.getContext(arg)
								if(argContext != null) {
									context = argContext
								} else {
									user = e.bot.users.fromNick(arg).user
								}
							}

							val allPermissions = context.checkPermission(e.user.user, 'allPermissions)
							val sharePermissions = context.checkPermission(e.user.user, 'sharePermissions)

							for(permStr <- permissions.split(',')) {
								val perm = Symbol(permStr)

								if(!(allPermissions || (context.checkPermission(e.user.user, perm) && sharePermissions))) {
									e.reply("Insufficient Permissions")

									return ()
								}

								context.grantPermission(user, perm)
								e.genericReply(s"Granted $perm to ${user.username} in $context")
							}

						case "take" =>
							var context = e.context
							var user = e.user.user
							val permissions = e.args(e.args.length - 1)

							for(arg <- e.args.view(1, e.args.length - 1)) {
								val argContext = e.bot.getContext(arg)
								if(argContext != null) {
									context = argContext
								} else {
									user = e.bot.users.fromNick(arg).user
								}
							}

							val allPermissions = context.checkPermission(e.user.user, 'allPermissions)
							val takeSamePerms = context.checkPermission(e.user.user, 'takePermissions)

							for(permStr <- permissions.split(',')) {
								val perm = Symbol(permStr)

								if(!(allPermissions || (context.checkPermission(e.user.user, perm) && takeSamePerms))) {
									e.reply("Insufficient Permissions")

									return ()
								}

								context.takePermission(user, perm)
								e.genericReply(s"Took $perm from ${user.username} in $context")
							}

						case _ =>
							printUsage()
					}
				}

				()
			}
		)
	}
}

class OPPlugin extends Plugin {
	@Subscribe
	def op(e: TCommandCheckEvent) {
		e.command(
			name = "op",
			usage = "[channel] [user]",
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				var user = e.user.nick
				var channel: Channel = null

				e match {
					case event: ChannelEvent =>
						channel = event.channel
					case _ =>
				}

				if(channel == null || e.args.length >= 2 || (e.args.length >= 1 && e.args(0).startsWith("#"))){
					if(e.args.length >= 1) {
						channel = e.bot.channels(e.args(0))
					} else {
						printUsage()
					}

					if(e.args.length >= 2) {
						user = e.args(1)
					}
				} else {
					if(e.args.length >= 1) {
						user = e.args(0)
					}
				}

				if(!(channel.checkPermission(e.user.user, 'opOthers) ||
					(e.user.nick == user && channel.checkPermission(e.user.user, 'opSelf)))) {

					e.reply("Insufficient Permissions")
					return ()
				}

				e.bot.users.fromNick("ChanServ").send.msg(s"op #${channel.name} $user")
				e.genericReply(s"Opping $user in $channel")
				()
			}
		)
	}

	@Subscribe
	def deop(e: TCommandCheckEvent) {
		e.command(
			name = "deop",
			usage = "[channel] [user]",
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				var user = e.user.nick
				var channel: Channel = null
				var requiresChannel = false

				e match {
					case event: ChannelEvent =>
						channel = event.channel
					case _ =>
				}

				if(channel == null || e.args.length >= 2 || (e.args.length >= 1 && e.args(0).startsWith("#"))){
					if(e.args.length >= 1) {
						channel = e.bot.channels(e.args(0))
					} else {
						printUsage()
					}

					if(e.args.length >= 2) {
						user = e.args(1)
					}
				} else {
					if(e.args.length >= 1) {
						user = e.args(0)
					}
				}

				if(!(channel.checkPermission(e.user.user, 'deopOthers) ||
					(e.user.nick == user && channel.checkPermission(e.user.user, 'deopSelf)))) {

					e.reply("Insufficient Permissions")
					return ()
				}

				e.bot.users.fromNick("ChanServ").send.msg(s"deop #${channel.name} $user")
				e.genericReply(s"Deopping $user in $channel")
				()
			}
		)
	}
}

class SayHelloPlugin extends Plugin {
	@Subscribe
	def sayHi(e: TCommandCheckEvent) {
		e.command(
			name = "say-hi",
			usage = "[name]",
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					e.genericReply(s"HI ${e.user.nick}!")
				} else {
					e.genericReply(s"HI ${e.args(0)}!")
				}
			}
		)
	}
}

class EchoPlugin extends Plugin {
	@Subscribe
	def onMessage(e: ChannelMessageEvent) {
		if(e.user != e.bot.ircUser) {
			e.channel.send.msg(s"<${e.user.nick}> ${e.msg}")
		}
	}
}

class SayPlugin extends Plugin {
	@Subscribe
	def say(e: TCommandCheckEvent) {
		e.command(
			name = "say",
			usage = "<msg>...",
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					printUsage()
				} else {
					e.genericReply(e.args.mkString(" "))
				}

				()
			}
		)
	}
}
