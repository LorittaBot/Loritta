package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.utils.extensions.objectNode

class PingCommand : AbstractCommand("ping", category = CommandCategory.MISC) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["PING_DESCRIPTION"]
    }

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "shards") {
			if (loritta.config.socket.enabled) {
				val networkShards = loritta.socket.socketWrapper!!.awaitResponse(SocketOpCode.Discord.GET_LORITTA_SHARDS, objectNode())["lorittaShards"]

				val row0 = mutableListOf<String>()
				val row1 = mutableListOf<String>()
				val row2 = mutableListOf<String>()
				val row3 = mutableListOf<String>()
				val row4 = mutableListOf<String>()

				networkShards.forEach {
					val totalGuildCount = it["shards"].sumBy {
						it["guildCount"].intValue()
					}
					val totalUserCount = it["shards"].sumBy {
						it["userCount"].intValue()
					}

					row0.add("Loritta Shard ${it["lorittaShardId"].intValue()} (${it["lorittaShardName"].textValue()})")
					row1.add("---")
					row2.add("---")
					row3.add("$totalGuildCount guilds")
					row4.add("$totalUserCount users")

					it["shards"].sortedBy { it["id"].intValue() }.forEach {
						row0.add("> Shard ${it["id"].intValue()}")
						row1.add(it["gatewayPing"].longValue().toString() + "ms") // gateway ping
						row2.add(it["status"].textValue())
						row3.add(it["guildCount"].intValue().toString() + " guilds") // guilds
						row4.add(it["userCount"].intValue().toString() + " users") // users
					}
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
					context.sendMessage("```$str```")
				}
			} else {
				val row0 = mutableListOf<String>()
				val row1 = mutableListOf<String>()
				val row2 = mutableListOf<String>()
				val row3 = mutableListOf<String>()
				val row4 = mutableListOf<String>()

				lorittaShards.getShards().sortedBy { it.shardInfo?.shardId }.forEach {
					row0.add("Shard ${it.shardInfo?.shardId}")
					row1.add("${it.gatewayPing}ms")
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
					context.sendMessage("```$str```")
				}
			}
		} else {
            val time = System.currentTimeMillis()

            val replies = mutableListOf(
					LoriReply(
							message = "**Pong!** (\uD83D\uDCE1 Shard ${context.event.jda.shardInfo?.shardId}/${loritta.discordConfig.discord.maxShards - 1})",
							prefix = ":ping_pong:"
					),
					LoriReply(
							message = "**Gateway Ping: ** `${context.event.jda.gatewayPing}ms`",
							prefix = ":stopwatch:",
							mentionUser = false
					),
					LoriReply(
							message = "**API Ping:** `...ms`",
							prefix = ":stopwatch:",
							mentionUser = false
					)
			)

			val message = context.reply(*replies.toTypedArray())

			replies.removeAt(2) // remova o último
			replies.add(
					LoriReply(
							message = "**API Ping:** `${System.currentTimeMillis() - time}ms`",
							prefix = ":zap:",
							mentionUser = false
					)
			)

			message.editMessage(replies.joinToString(separator = "\n", transform = {it.build(context)})).await()

			message.onReactionAddByAuthor(context) {
				message.editMessage("${context.userHandle.asMention} i luv u <:lori_blobnom:412582340272062464>").queue()
			}
		}
	}
}