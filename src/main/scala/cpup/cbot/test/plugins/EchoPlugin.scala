package cpup.cbot.test.plugins

import cpup.cbot.plugin.SingletonPlugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.ChannelMessageEvent

object EchoPlugin extends SingletonPlugin {
	def name = "echo"

	@Subscribe
	def onMessage(e: ChannelMessageEvent) {
		if(e.ircUser != e.bot.ircUser) {
			e.channel.send.msg(s"<${e.ircUser.nick}> ${e.msg}")
		}
	}
}