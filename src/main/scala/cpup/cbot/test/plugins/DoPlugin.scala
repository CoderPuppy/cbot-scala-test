package cpup.cbot.test.plugins

import cpup.cbot.plugin.SingletonPlugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.{CBot, Context}
import cpup.cbot.events.Replyable
import cpup.cbot.users.IRCUser

object DoPlugin extends SingletonPlugin {
	def name = "do"

	@Subscribe
	def doCommand(e: TCommandCheckEvent) {
		e.command(
			name = "do",
			usage = "<context> <cmd> <args>...",
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 2) {
					printUsage()
				} else {
					val context = e.bot.getContext(e.args(0))
					val cmd = e.args(1)
					val args = e.args.view(2, e.args.length)

					e.reply(s"Doing ${'"'}$cmd ${args.mkString(" ")}${'"'} in $context")

					val doEvent = new DoCommandEvent(
						e.bot,
						context,
						e,
						e.ircUser,
						cmd,
						args
					)
					e.bot.bus.post(doEvent)
//					e.context.bus.post(doEvent)
				}
			}
		)
	}

	case class DoCommandEvent(bot: CBot, override val context: Context, reply: Replyable, ircUser: IRCUser, cmd: String, args: Seq[String]) extends TCommandEvent {
		override def privateReply(msg: String) {
			reply.privateReply(msg)
		}
		override def genericReply(msg: String) {
			reply.genericReply(msg)
		}
		override def reply(msg: String) {
			reply.reply(msg)
		}
	}
}