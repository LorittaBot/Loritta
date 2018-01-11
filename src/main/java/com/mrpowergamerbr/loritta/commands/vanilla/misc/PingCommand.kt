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
			val embed = EmbedBuilder().apply {
				setColor(Constants.LORITTA_AQUA)
				setTitle("\uD83C\uDF0E ${locale["PING_ShardsInfo"]}")

				for (shard in lorittaShards.shards) {
					val lastUpdate = lorittaShards.lastJdaEventTime.getOrDefault(shard, System.currentTimeMillis())
					val seconds = (System.currentTimeMillis() - lastUpdate) / 1000
					addField("Shard ${shard.shardInfo.shardId}", "${shard.ping}ms - ${seconds}s", true)
				}
			}

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			val message = context.reply(
					LoriReply(
							message = "**Pong!** `${context.event.jda.ping}ms` (\uD83C\uDF0E Shard ${context.event.jda.shardInfo.shardId}/${Loritta.config.shards - 1})",
							prefix = "\uD83C\uDFD3"
					)
			)

			message.onReactionAddByAuthor(context) {
				message.editMessage("${context.userHandle.asMention} i luv u <:blobNom:357976954688897026>").queue()
			}
		}
    }
}