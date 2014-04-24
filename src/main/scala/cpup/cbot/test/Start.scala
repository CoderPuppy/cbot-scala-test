package cpup.cbot.test

import cpup.cbot.{CBot, BotConfig}
import cpup.cbot.plugin._
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.ChannelEvent
import cpup.cbot.plugin.CommandPlugin.{TCommandCheckEvent, TCommandEvent}
import cpup.cbot.channels.Channel
import cpup.cbot.events.channel.ChannelMessageEvent

object Start {
	def main(args: Array[String]) {
		val config = new BotConfig.Builder("localhost", "cbot")
			.setAutoReconnect(true)

		val bot = new CBot(config)

		val pluginManagement = new PluginManagementPlugin(Map(
			"echo" -> new EchoPlugin,
			"say-hello" -> new SayHelloPlugin,
			"say" -> new SayPlugin,
			"channel-management" -> new ChannelManagementPlugin,
			"op" -> new OPPlugin,
			"help" -> new HelpPlugin
		))

		bot
			.enablePlugin(new CommandPlugin("!"))
			.enablePlugin(pluginManagement)

		bot.channels.join("code/cbot")
			.setRejoin(true)
			.enablePlugin(pluginManagement.plugins("op"))
			.enablePlugin(pluginManagement.plugins("help"))
			.enablePlugin(pluginManagement.plugins("channel-management"))

		bot.connect
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
				var channel: String = null
				var requiresChannel = false

				e match {
					case event: ChannelEvent =>
						channel = event.channel.name
						requiresChannel = true
					case _ =>
				}

				if(channel == null || e.args.length >= 2 || (e.args.length >= 1 && e.args(0).startsWith("#"))){
					if(e.args.length >= 1) {
						channel = Channel.unifyName(e.args(0))
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

				e.bot.users.fromNick("ChanServ").send.msg(s"op #$channel $user")
				e.genericReply(s"Opping $user in #$channel")
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
				var channel: String = null
				var requiresChannel = false

				e match {
					case event: ChannelEvent =>
						channel = event.channel.name
						requiresChannel = true
					case _ =>
				}

				if(channel == null || e.args.length >= 2 || (e.args.length >= 1 && e.args(0).startsWith("#"))){
					if(e.args.length >= 1) {
						channel = Channel.unifyName(e.args(0))
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

				e.bot.users.fromNick("ChanServ").send.msg(s"deop #$channel $user")
				e.genericReply(s"Deopping $user in #$channel")
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
		if(e.user != e.bot.user) {
			e.channel.send.msg(e.msg)
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