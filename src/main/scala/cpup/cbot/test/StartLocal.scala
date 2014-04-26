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

object StartLocal extends BasicStart {
	lazy val config = new BotConfig.Builder("localhost", "cbot")
		.setAutoReconnect(true)
		.setNickServPass("imacbot")
		.finish
	lazy val saveFile = new File("local.json")

	def main(args: Array[String]) {
		bot.channels.join("code/cbot")
			.setRejoin(true)
			.enablePlugin(pluginManagement.plugins("op"))

		bot.connect
	}
}