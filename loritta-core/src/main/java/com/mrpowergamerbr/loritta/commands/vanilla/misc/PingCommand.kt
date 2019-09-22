package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.JDA
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.util.concurrent.TimeUnit

class PingCommand : AbstractCommand("ping", category = CommandCategory.MISC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PING_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "shards" || arg0 == "clusters") {
			val results = lorittaShards.queryAllLorittaClusters("/api/v1/loritta/status")

			val row0 = mutableListOf<String>()
			val row1 = mutableListOf<String>()
			val row2 = mutableListOf<String>()
			val row3 = mutableListOf<String>()
			val row4 = mutableListOf<String>()

			results.forEach {
				try {
					val json = it.await()

					val shardId = json["id"].long
					val name = json["name"].string

					val totalGuildCount = json["shards"].array.sumBy { it["guildCount"].int }
					val totalUserCount = json["shards"].array.sumBy { it["userCount"].int }

					var jvmUpTime = json["uptime"].long
					val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
					jvmUpTime -= TimeUnit.DAYS.toMillis(days)
					val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
					jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
					val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
					jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
					val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

					val sb = StringBuilder(64)
					sb.append(days)
					sb.append("d ")
					sb.append(hours)
					sb.append("h ")
					sb.append(minutes)
					sb.append("m ")
					sb.append(seconds)
					sb.append("s")

					val pingAverage = json["shards"].array.map { it["ping"].int }.average().toInt() // arredondar

					row0.add("Loritta Cluster $shardId ($name)")
					row1.add("~${pingAverage}ms")
					row2.add(sb.toString())
					row3.add("$totalGuildCount guilds")
					row4.add("$totalUserCount users")

					val unstableShards = json["shards"].array.filter {
						it["status"].string != JDA.Status.CONNECTED.toString() || it["ping"].int == -1 || it["ping"].int >= 250
					}

					if (unstableShards.isNotEmpty()) {
						row0.add("* UNSTABLE SHARDS:")
						row1.add("---")
						row2.add("---")
						row3.add("---")
						row4.add("---")

						unstableShards.forEach {
							row0.add("> Shard ${it["id"].long}")
							row1.add("${it["ping"].int}ms")
							row2.add(it["status"].string)
							row3.add("${it["guildCount"].long} guilds")
							row4.add("${it["userCount"].long} users")
						}
					}
				} catch (e: ShardOfflineException) {
					row0.add("Loritta Cluster ${e.id} (${e.name})")
					row1.add("---")
					row2.add("---")
					row3.add("---")
					row4.add("OFFLINE!")
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
		} else if ((arg0 == "all_shards" || arg0 == "all_clusters") && (context.userHandle.support || loritta.config.isOwner(context.userHandle.idLong))) {
			val results = lorittaShards.queryAllLorittaClusters("/api/v1/loritta/status")

			val row0 = mutableListOf<String>()
			val row1 = mutableListOf<String>()
			val row2 = mutableListOf<String>()
			val row3 = mutableListOf<String>()
			val row4 = mutableListOf<String>()

			results.forEach {
				try {
					val json = it.await()

					val shardId = json["id"].long
					val name = json["name"].string

					val totalGuildCount = json["shards"].array.sumBy { it["guildCount"].int }
					val totalUserCount = json["shards"].array.sumBy { it["userCount"].int }

					var jvmUpTime = json["uptime"].long
					val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
					jvmUpTime -= TimeUnit.DAYS.toMillis(days)
					val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
					jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
					val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
					jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
					val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

					val sb = StringBuilder(64)
					sb.append(days)
					sb.append("d ")
					sb.append(hours)
					sb.append("h ")
					sb.append(minutes)
					sb.append("m ")
					sb.append(seconds)
					sb.append("s")

					row0.add("Loritta Cluster $shardId ($name)")
					row1.add("---")
					row2.add(sb.toString())
					row3.add("$totalGuildCount guilds")
					row4.add("$totalUserCount users")

					json["shards"].array.forEach {
						row0.add("> Shard ${it["id"].long}")
						row1.add("${it["ping"].int}ms")
						row2.add(it["status"].string)
						row3.add("${it["guildCount"].long} guilds")
						row4.add("${it["userCount"].long} users")
					}

				} catch (e: ShardOfflineException) {
					row0.add("Loritta Cluster ${e.id} (${e.name})")
					row1.add("---")
					row2.add("---")
					row3.add("---")
					row4.add("OFFLINE!")
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
			val time = System.currentTimeMillis()

			val replies = mutableListOf(
					LoriReply(
							message = "**Pong!** (\uD83D\uDCE1 Shard ${context.event.jda.shardInfo?.shardId}/${loritta.discordConfig.discord.maxShards - 1}) (<:loritta:331179879582269451> Loritta Cluster ${loritta.lorittaCluster.id} (`${loritta.lorittaCluster.name}`))",
							prefix = ":ping_pong:"
					),
					LoriReply(
							message = "**Gateway Ping:** `${context.event.jda.gatewayPing}ms`",
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

	data class ShardOfflineException(val id: Long, val name: String) : RuntimeException()
}