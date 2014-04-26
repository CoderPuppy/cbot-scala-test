package cpup.cbot.test

import cpup.cbot.plugin._
import java.io.File
import cpup.cbot.{CBot, BotConfig}

trait BasicStart {
	val pluginTypes = PluginType.pluginTypes(
		ChannelManagementPlugin,
		EchoPlugin,
		SayHelloPlugin,
		SayPlugin,
		OPPlugin,
		HelpPlugin,
		UsersPlugin,
		PermsPlugin,
		CommandPlugin,
		SavingPlugin,
		PluginManagementPlugin
	)

	def saveFile: File
	def config: BotConfig

	val pluginManagement = new PluginManagementPlugin(pluginTypes)

	val bot = new CBot(config)

	if(saveFile.exists) {
		SavingPlugin.load(bot, pluginTypes, saveFile)
	}

	bot.enablePlugin(pluginManagement)
}