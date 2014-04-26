package cpup.cbot.test

import cpup.cbot.{CBot, BotConfig}
import cpup.cbot.plugin._
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.ChannelEvent
import cpup.cbot.plugin.CommandPlugin.{TCommandCheckEvent, TCommandEvent}
import cpup.cbot.channels.Channel
import cpup.cbot.events.channel.ChannelMessageEvent
import cpup.cbot.users.{AlreadyRegisteredException, GuestUserException, IncorrectPasswordException, UnknownUserException}
import java.io.File

object StartLocal {
	def main(args: Array[String]) {
		val config = new BotConfig.Builder("localhost", "cbot")
			.setAutoReconnect(true)
			.setNickServPass("imacbot")

		val bot = new CBot(config)

		val saveFile = new File("local.json")

		if(saveFile.exists) {
			SavingPlugin.load(bot, saveFile)
		}

		val pluginManagement = new PluginManagementPlugin(Map(
			"echo" -> new EchoPlugin,
			"say-hello" -> new SayHelloPlugin,
			"say" -> new SayPlugin,
			"channel-management" -> new ChannelManagementPlugin,
			"op" -> new OPPlugin,
			"help" -> new HelpPlugin,
			"users" -> new UsersPlugin
		))

		bot
			.enablePlugin(new CommandPlugin("!"))
			.enablePlugin(pluginManagement)
			.enablePlugin(new SavingPlugin(saveFile))
			.enablePlugin(pluginManagement.plugins("help"))
			.enablePlugin(pluginManagement.plugins("users"))
			.enablePlugin(pluginManagement.plugins("channel-management"))

		bot.channels.join("code/cbot")
			.setRejoin(true)
			.enablePlugin(pluginManagement.plugins("op"))

		bot.connect
	}
}