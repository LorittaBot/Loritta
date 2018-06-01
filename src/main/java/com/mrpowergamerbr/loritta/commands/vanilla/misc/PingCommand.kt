package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder

class PingCommand : AbstractCommand("ping", category = CommandCategory.MISC) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["PING_DESCRIPTION"]
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "shards") {
			val row0 = mutableListOf<String>()
			val row1 = mutableListOf<String>()
			val row2 = mutableListOf<String>()
			val row3 = mutableListOf<String>()
			val row4 = mutableListOf<String>()

			lorittaShards.shards.sortedBy { it.shardInfo.shardId }.forEach {
				row0.add("Shard ${it.shardInfo.shardId}")
				row1.add("${it.ping}ms")
				row2.add(it.status.name)
				row3.add("${it.guilds.size} guilds")
				row4.add("${it.users.size} users")
			}

			val maxRow0 = row0.maxBy { it.length }!!.length
			val maxRow1 = row1.maxBy { it.length }!!.length
			val maxRow2 = row2.maxBy { it.length }!!.length
			val maxRow3 = row3.maxBy { it.length }!!.length
			val maxRow4 = row4.maxBy { it.length }!!.length

			val lines = mutableListOf<String>()
			for (i in 0 until row0.size) {
				val arg0 = row0.getOrNull(i) ?: "---"
				val arg1 = row1.getOrNull(i) ?: "---"
				val arg2 = row2.getOrNull(i) ?: "---"
				val arg3 = row3.getOrNull(i) ?: "---"
				val arg4 = row4.getOrNull(i) ?: "---"

				lines += "${arg0.padEnd(maxRow0, ' ')} | ${arg1.padEnd(maxRow1, ' ')} | ${arg2.padEnd(maxRow2, ' ')} | ${arg3.padEnd(maxRow3, ' ')} | ${arg4.padEnd(maxRow4, ' ')}"
			}

			val asMessage = mutableListOf<String>()

			var buf = ""
			for (aux in lines) {
				if (buf.length + aux.length > 1900) {
					asMessage.add(buf)
					buf = ""
				}
				buf += aux + "\n"
			}

			asMessage.add(buf)

			for (str in asMessage) {
				context.sendMessage("```${str}```")
			}
		} else {
			val message = context.reply(
					LoriReply(
							message = "**Pong!** `${context.event.jda.ping}ms` (\uD83D\uDCE1 Shard ${context.event.jda.shardInfo.shardId}/${Loritta.config.shards - 1})",
							prefix = "\uD83C\uDFD3"
					)
			)

			message.onReactionAddByAuthor(context) {
				message.editMessage("${context.userHandle.asMention} i luv u <:lori_blobnom:412582340272062464>").queue()
			}
		}
    }
}