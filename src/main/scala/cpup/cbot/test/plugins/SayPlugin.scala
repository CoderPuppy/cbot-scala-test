package cpup.cbot.test.plugins

import cpup.cbot.plugin.SingletonPlugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.commandPlugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.plugin.commandPlugin.{TCommand, BasicArguments, BasicCommand}

object SayPlugin extends SingletonPlugin {
	def name = "say"

	@Subscribe
	def say(e: TCommandCheckEvent) {
		e.command(new TCommand[String] {
			override def name = "say"

			override def usages = List("<-msg->")

			override def parse(e: TCommandEvent) = e.args.positional.mkString(" ")

			override def handle(e: TCommandEvent, msg: String) {
				e.context.output match {
					case Some(output) =>
						output.msg(msg)

					case None =>
						e.genericReply(msg)
				}
			}
		})
	}
}