package cpup.cbot.test

import cpup.cbot.plugin.{PluginManager, Plugin}
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.users._
import cpup.cbot.events.channel._
import cpup.cbot.channels.{ChannelWrites, Channel}
import java.io.{PrintWriter, File}
import play.api.libs.json.{JsValue, Json, JsObject}
import cpup.cbot.CBot
import cpup.cbot.events.user._
import cpup.cbot.events.channel.ChannelMessageEvent
import cpup.cbot.events.user.RegisterEvent
import cpup.cbot.events.channel.JoinEvent
import cpup.cbot.events.channel.ChannelUpdateEvent
import cpup.cbot.events.channel.LeaveEvent
import scala.io.Source

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
									e.ircUser.login(e.args(1), e.args(2))
									e.reply(s"Logged in as ${e.args(1)}")
								} catch {
									case ex: UnknownUserException =>
										e.reply(s"Unknown User: ${e.args(1)}")

									case ex: IncorrectPasswordException =>
										e.reply(s"Incorrect Password")
								}
							}

						case "logout" =>
							e.ircUser.logout

						case "setpass" | "setpassword" =>
							if(e.args.length < 2) {
								printUsage()
							} else {
								e.ircUser.user.password = User.hash(e.args(1))
								e.reply(s"Set password for ${e.ircUser.user.username}")
							}

						case "whois" =>
							var user = e.ircUser

							if(e.args.length >= 2) {
								user = e.bot.users.fromNick(e.args(1))
							}

							e.genericReply(s"--[===[${user.nick}]===]--")
							e.genericReply(s"Logged in as ${user.user.username}")

						case "register" =>
							if(e.args.length < 3) {
								printUsage()
							} else {
								if(!e.bot.checkPermission(e.ircUser.user, 'register)) {
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
								e.ircUser.registerNickServ
								e.reply(s"Registered NickServ account: ${e.ircUser.nickserv} to user: ${e.ircUser.user.username}")
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
							var user = e.ircUser.user

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
							var user = e.ircUser.user
							val permissions = e.args(e.args.length - 1)

							for(arg <- e.args.view(1, e.args.length - 1)) {
								val argContext = e.bot.getContext(arg)
								if(argContext != null) {
									context = argContext
								} else {
									user = e.bot.users.fromNick(arg).user
								}
							}

							val allPermissions = context.checkPermission(e.ircUser.user, 'allPermissions)
							val sharePermissions = context.checkPermission(e.ircUser.user, 'sharePermissions)

							for(permStr <- permissions.split(',')) {
								val perm = Symbol(permStr)

								if(!(allPermissions || (context.checkPermission(e.ircUser.user, perm) && sharePermissions))) {
									e.reply("Insufficient Permissions")

									return ()
								}

								context.grantPermission(user, perm)
								e.genericReply(s"Granted $perm to ${user.username} in $context")
							}

						case "take" =>
							var context = e.context
							var user = e.ircUser.user
							val permissions = e.args(e.args.length - 1)

							for(arg <- e.args.view(1, e.args.length - 1)) {
								val argContext = e.bot.getContext(arg)
								if(argContext != null) {
									context = argContext
								} else {
									user = e.bot.users.fromNick(arg).user
								}
							}

							val allPermissions = context.checkPermission(e.ircUser.user, 'allPermissions)
							val takeSamePerms = context.checkPermission(e.ircUser.user, 'takePermissions)

							for(permStr <- permissions.split(',')) {
								val perm = Symbol(permStr)

								if(!(allPermissions || (context.checkPermission(e.ircUser.user, perm) && takeSamePerms))) {
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
				var user = e.ircUser.nick
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

				if(!(channel.checkPermission(e.ircUser.user, 'opOthers) ||
					(e.ircUser.nick == user && channel.checkPermission(e.ircUser.user, 'opSelf)))) {

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
				var user = e.ircUser.nick
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

				if(!(channel.checkPermission(e.ircUser.user, 'deopOthers) ||
					(e.ircUser.nick == user && channel.checkPermission(e.ircUser.user, 'deopSelf)))) {

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
					e.genericReply(s"HI ${e.ircUser.nick}!")
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
		if(e.ircUser != e.bot.ircUser) {
			e.channel.send.msg(s"<${e.ircUser.nick}> ${e.msg}")
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

class SavingPlugin(val file: File) extends Plugin {
	var channels = Json.obj()
	var users = Json.obj()
	var nickServUsers = Json.obj()

	implicit val channelWrites = ChannelWrites
	implicit val userWrites = UserWrites

	def save {
		val json = Json.prettyPrint(Json.obj(
			"channels" -> channels,
			"users" -> users,
			"nickServUsers" -> nickServUsers
		))
		val writer = new PrintWriter(file)
		writer.write(json)
		writer.close
	}

	def updateChannel(chan: Channel) {
		channels -= chan.name
		channels += (chan.name -> Json.toJson(chan))
	}

	def updateUser(user: User) {
		users -= user.username
		users += (user.username -> Json.toJson(user))
	}

	override def enable(manager: PluginManager) = {
		if(!managers.isEmpty) {
			throw new AlreadyRegisteredException("already registered")
		}

		manager match {
			case bot: CBot =>
				super.enable(manager)
				channels = Json.toJson(bot.channels.current.map((chan) => {
					(chan.name, chan)
				}).toMap).asInstanceOf[JsObject]
				users = Json.toJson(bot.users.registeredUsers.toMap).asInstanceOf[JsObject]
				nickServUsers = Json.toJson(bot.users.nickServUsers.toMap.map((kv) => {
					(kv._1, kv._2.username)
				})).asInstanceOf[JsObject]

			case _ =>
				throw new ClassCastException("SavingPlugin only works for CBots")
		}
	}

	override def disable(manager: PluginManager) = {
		super.disable(manager)
		channels = Json.obj()
		users = Json.obj()
	}

	@Subscribe
	def join(e: JoinEvent) {
		updateChannel(e.channel)
		save
	}

	@Subscribe
	def leave(e: LeaveEvent) {
		channels -= e.channel.name
		save
	}

	@Subscribe
	def channelUpdate(e: ChannelUpdateEvent) {
		updateChannel(e.channel)
		save
	}

	@Subscribe
	def userUpdate(e: UserUpdateEvent) {
		updateUser(e.user)
		save
	}

	@Subscribe
	def registerNickServ(e: RegisterNickServEvent) {
		nickServUsers += (e.nickserv -> Json.toJson(e.user.username))
	}

	@Subscribe
	def register(e: RegisterEvent) {
		updateUser(e.user)
		save
	}

	// TODO: use this if unregistering is implemented
//	@Subscribe
//	def unregister(e: UnregisterEvent) {
//		users -= e.user.username
//		save
//	}
}
object SavingPlugin {
	def load(bot: CBot, file: File) {
		val json: JsValue = Json.parse({
			val source = Source.fromFile(file)
			val contents = source.mkString
			source.close
			contents
		})

		(json \ "channels").as[Map[String, JsObject]].values.foreach((json) => {
			bot.channels.join(
				(json \ "name").as[String],
				(json \ "key").validate[String].getOrElse(null)
			).setRejoin((json \ "rejoin").as[Boolean])
		})

		(json \ "users").as[Map[String, JsObject]].values.foreach((userJSON) => {
			val user = bot.users.register(
				(userJSON \ "username").as[String],
				(userJSON \ "password").validate[String].getOrElse(null)
			)

			(userJSON \ "permissions").as[List[String]].foreach((perm) => {
				user.grantPermission(Symbol(perm))
			})

			(userJSON \ "channelPermissions").as[Map[String, List[String]]].foreach((kv) => {
				val (chan, perms) = kv
				perms.foreach((perm) => {
					user.grantPermission(chan, Symbol(perm))
				})
			})
		})

		(json \ "nickServUsers").as[Map[String, String]].foreach((kv) => {
			bot.users.registerNickServ(kv._1, bot.users(kv._2))
		})
	}
}