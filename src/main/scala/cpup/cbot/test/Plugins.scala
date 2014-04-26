package cpup.cbot.test

import cpup.cbot.plugin.SingletonPlugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.events.channel._
import cpup.cbot.channels.Channel
import cpup.cbot.events.channel.ChannelMessageEvent

object OPPlugin extends SingletonPlugin {
	def name = "op" // TODO: IRCUsersPlugin?

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

object SayHelloPlugin extends SingletonPlugin {
	def name = "say-hello"

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

object EchoPlugin extends SingletonPlugin {
	def name = "echo"

	@Subscribe
	def onMessage(e: ChannelMessageEvent) {
		if(e.ircUser != e.bot.ircUser) {
			e.channel.send.msg(s"<${e.ircUser.nick}> ${e.msg}")
		}
	}
}

object SayPlugin extends SingletonPlugin {
	def name = "say"

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


