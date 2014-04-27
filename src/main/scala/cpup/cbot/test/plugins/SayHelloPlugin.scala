package cpup.cbot.test.plugins

import cpup.cbot.plugin.SingletonPlugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}

object SayHelloPlugin extends SingletonPlugin {
	def name = "say-hi"

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