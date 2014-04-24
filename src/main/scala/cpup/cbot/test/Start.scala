package cpup.cbot.test

import cpup.cbot.{CBot, BotConfig}
import cpup.cbot.plugin.{CommandPlugin, Plugin}
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.{ChannelEvent, ChannelMessageEvent}
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, CommandEvent}

object Start {
	def main(args: Array[String]) {
		val config = new BotConfig.Builder("localhost", "cbot")
		config.autoReconnect = true

		val bot = new CBot(config)
		bot.channels.join("code/cbot")
			.setRejoin(true)
			.enablePlugin(new PluginManagementPlugin)
			.enablePlugin(new SayHelloPlugin)
			.enablePlugin(new EchoPlugin)
			.enablePlugin(new CommandPlugin("!"))
		bot.connect
	}
}

class SayHelloPlugin extends Plugin {
	@Subscribe
	def sayHi(e: TCommandEvent) {
		if(e.cmd == "say-hi") {
			if(e.args.length < 1) {
				e.reply(s"Usage: ${e.cmd} <name>")
			} else {
				e.genericReply(s"HI ${e.args(0)}!")
			}
		}
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

class PluginManagementPlugin extends Plugin {
	@Subscribe
	def listPlugins(e: TCommandEvent) {
		if(e.cmd == "list-plugins") {
			e.reply(s"Enabled Plugins: ${e.pluginManager.plugins.mkString(", ")}")
		}
	}

	@Subscribe
	def disablePlugin(e: TCommandEvent) {
		if(e.cmd == "disable-plugin") {
			if(e.args.length < 1) {
				e.reply(s"Usage: ${e.cmd} <classname>")
			} else {
				val filter = e.args(0)
				val plugins = e.pluginManager.plugins.filter(_.getClass.getName.contains(filter))
				e.genericReply(s"Disabling plugins: ${plugins.mkString(", ")}")
				plugins.foreach(e.pluginManager.disablePlugin(_))
			}
		}
	}
}