package cpup.cbot.test.plugins

import cpup.cbot.plugin.SingletonPlugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}

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
					e.context.output match {
						case Some(output) =>
							output.msg(e.args.mkString(" "))

						case None =>
							e.genericReply(e.args.mkString(" "))
					}
				}

				()
			}
		)
	}
}