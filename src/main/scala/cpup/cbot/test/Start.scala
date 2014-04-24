package cpup.cbot.test

import cpup.cbot.{CBot, BotConfig}
import cpup.cbot.plugin.{CommandPlugin, Plugin}
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.{ChannelEvent, ChannelMessageEvent}
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, CommandEvent}
import scala.collection.mutable
import scala.collection.immutable.HashMap

object Start {
	def main(args: Array[String]) {
		val config = new BotConfig.Builder("localhost", "cbot")
		config.autoReconnect = true

		val bot = new CBot(config)

		val pluginsManagement = new PluginManagementPlugin(Map(
			"command" -> new CommandPlugin("!"),
			"echo" -> new EchoPlugin,
			"say-hello" -> new SayHelloPlugin
		))
		pluginsManagement.registerPlugin("plugins-management", pluginsManagement)

		bot.channels.join("code/cbot")
			.setRejoin(true)
			.enablePlugin(pluginsManagement.plugins("command"))
			.enablePlugin(pluginsManagement.plugins("plugins-management"))
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

class PluginManagementPlugin(protected var _plugins: Map[String, Plugin]) extends Plugin {
	protected var reversePlugins = plugins.map(_.swap)

	def plugins = _plugins

	def registerPlugin(name: String, plugin: Plugin) = {
		_plugins += name -> plugin
		reversePlugins += plugin -> name
		this
	}

	def convertToName(pl: Plugin) = reversePlugins.getOrElse(pl, pl.toString)

	@Subscribe
	def listPlugins(e: TCommandEvent) {
		if(e.cmd == "plugins") {
			val enabledPlugins = e.pluginManager.plugins.map(convertToName)
			e.reply(s"Enabled Plugins: ${enabledPlugins.mkString(", ")}")
			e.reply(s"Avaliable Plugins: ${(plugins.keySet -- enabledPlugins).mkString(", ")}")
		}
	}

	@Subscribe
	def enablePlugin(e: TCommandEvent) {
		if(e.cmd == "enable-plugin") {
			if(e.args.length < 1) {
				e.reply(s"Usage: ${e.cmd} <plugin>")
			} else {
				val filter = e.args(0).toLowerCase
				val plugins = _plugins.keySet.filter(_.toLowerCase.contains(filter)).map(_plugins(_))
				if(plugins.isEmpty) {
					e.reply(s"No plugins matched: $filter}")
				} else {
					e.reply(s"Enabling plugins: ${plugins.map(convertToName).mkString(", ")}")
					plugins.foreach(e.pluginManager.enablePlugin(_))
				}
			}
		}
	}

	@Subscribe
	def disablePlugin(e: TCommandEvent) {
		if(e.cmd == "disable-plugin") {
			if(e.args.length < 1) {
				e.reply(s"Usage: ${e.cmd} <plugin>")
			} else {
				val filter = e.args(0).toLowerCase
				val plugins = e.pluginManager.plugins.map((pl) => {
					(pl, convertToName(pl))
				}).filter({
					_._2.toLowerCase.contains(filter)
				})
				if(plugins.isEmpty) {
					e.reply(s"No plugins matched: $filter}")
				} else {
					e.genericReply(s"Disabling plugins: ${plugins.map(_._2).mkString(", ")}")
					plugins.foreach((t) => e.pluginManager.disablePlugin((t._1)))
				}
			}
		}
	}
}