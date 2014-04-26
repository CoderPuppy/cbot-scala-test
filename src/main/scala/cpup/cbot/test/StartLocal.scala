package cpup.cbot.test

import cpup.cbot.BotConfig
import cpup.cbot.plugin.CommandPlugin.TCommandCheckEvent
import cpup.cbot.channels.Channel
import cpup.cbot.events.channel.ChannelMessageEvent
import cpup.cbot.users.GuestUserException
import java.io.File

object StartLocal extends BasicStart {
	lazy val config = new BotConfig.Builder("localhost", "cbot")
		.setAutoReconnect(true)
		.setNickServPass("imacbot")
		.finish
	lazy val saveFile = new File("local.json")

	def main(args: Array[String]) {
		bot.channels.join("code/cbot")
			.setRejoin(true)
			.enablePlugin(pluginTypes, "op")

		bot.connect
	}
}