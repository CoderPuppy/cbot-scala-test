package cpup.cbot.test.plugins

import cpup.cbot.plugin.SingletonPlugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.commandPlugin.CommandPlugin.{TCommandCheckEvent, TCommandEvent}
import cpup.cbot.plugin.commandPlugin.{ArgResult, ArgDef, BasicArguments, BasicCommand}

object SayHelloPlugin extends SingletonPlugin {
	def name = "say-hi"

	@Subscribe
	def sayHi(e: TCommandCheckEvent) {
		e.command(new BasicCommand {
			override def name = "say-hi"

			val nameA = ArgDef.str.default((bot, context, ircUser) => ArgResult.from(ircUser.nick)).withName("name")
			override def args = List(nameA)

			override def handle(e: TCommandEvent, args: BasicArguments) {
				e.genericReply(s"HI ${args(nameA)}!")
			}
		})
	}
}