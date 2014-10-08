package cpup.cbot.test.plugins

import cpup.cbot.plugin.SingletonPlugin
import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.commandPlugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}

object OPPlugin extends SingletonPlugin {
	def name = "op" // TODO: IRCUsersPlugin?

	@Subscribe
	def op(e: TCommandCheckEvent) {
		/*e.command(
			name = "op",
			usage = "[channel] [user]",
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				/*var user = e.ircUser.nick
				var channel: Channel = null

				e.context match {
					case chan: Channel =>
						channel = chan

					case _ =>
				}

				if(channel == null || e.args.length >= 2 || (e.args.length >= 1 && e.args(0).startsWith("#"))){
					if(e.args.length >= 1) {
						channel = e.bot.channels(e.args(0))
					} else {
						printUsage()
					}

					if(e.args.length >= 2) {
						user = e.args(1)
					}
				} else {
					if(e.args.length >= 1) {
						user = e.args(0)
					}
				}

				if(!(channel.checkPermission(e.ircUser.user, 'opOthers) ||
					(e.ircUser.nick == user && channel.checkPermission(e.ircUser.user, 'opSelf)))) {

					e.reply("Insufficient Permissions")
					return ()
				}

				e.bot.users.fromNick("ChanServ").send.msg(s"op #${channel.name} $user")
				e.genericReply(s"Opping $user in $channel")
				()*/
			}
		)*/
	}

	@Subscribe
	def deop(e: TCommandCheckEvent) {
		/*e.command(
			name = "deop",
			usage = "[channel] [user]",
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				/*var user = e.ircUser.nick
				var channel: Channel = null

				e.context match {
					case chan: Channel =>
						channel = chan

					case _ =>
				}

				if(channel == null || e.args.length >= 2 || (e.args.length >= 1 && e.args(0).startsWith("#"))){
					if(e.args.length >= 1) {
						channel = e.bot.channels(e.args(0))
					} else {
						printUsage()
					}

					if(e.args.length >= 2) {
						user = e.args(1)
					}
				} else {
					if(e.args.length >= 1) {
						user = e.args(0)
					}
				}

				if(!(channel.checkPermission(e.ircUser.user, 'deopOthers) ||
					(e.ircUser.nick == user && channel.checkPermission(e.ircUser.user, 'deopSelf)))) {

					e.reply("Insufficient Permissions")
					return ()
				}

				e.bot.users.fromNick("ChanServ").send.msg(s"deop #${channel.name} $user")
				e.genericReply(s"Deopping $user in $channel")
				()*/
			}
		)*/
	}
}