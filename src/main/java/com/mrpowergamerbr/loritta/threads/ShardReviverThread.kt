package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.log
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlin.concurrent.thread

class ShardReviverThread : Thread("Shard Reviver") {
	override fun run() {
		super.run()

		while (true) {
			try {
				checkAndReviveDeadShards()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(1000)
		}
	}

	fun checkAndReviveDeadShards() {
		try {
			for (shard in lorittaShards.shards) {
				val lastUpdate = lorittaShards.lastJdaEventTime.getOrDefault(shard, System.currentTimeMillis())

				val seconds = (System.currentTimeMillis() - lastUpdate) / 1000

				if (seconds >= 10) {
					log("[SHARD] Shard ${shard.shardInfo.shardId} não recebeu update a mais de 10s! ~  ${seconds}s")
					println("[!] Shard ${shard.shardInfo.shardId} não recebeu update a mais de 10s! ~  ${seconds}s")
				}
			}

			val deadShards = lorittaShards.shards.filter {
				val lastUpdate = lorittaShards.lastJdaEventTime.getOrDefault(it, System.currentTimeMillis())

				System.currentTimeMillis() - lastUpdate > 60000
			}

			if (deadShards.isNotEmpty()) {
				for (deadShard in deadShards) {
					println("Reiniciando shard ${deadShard.shardInfo.shardId}...")
					log("[SHARD] Reiniciando shard ${deadShard.shardInfo.shardId}...")
					val shardId = deadShard.shardInfo.shardId

					lorittaShards.shards.remove(deadShard)
					lorittaShards.lastJdaEventTime.remove(deadShard)

					for (guild in deadShard.guilds) {
						loritta.songThrottle.remove(guild.id)
						loritta.musicManagers.remove(guild.idLong)
						loritta.discordListener.executors.remove(guild)
					}

					var guild = loritta.lorittaShards.getGuildById("297732013006389252")
					if (guild != null) {
						val textChannel = guild.getTextChannelById("297732013006389252")

						if (textChannel != null)
							textChannel.sendMessage("⚠ **|** Shard ${deadShard.shardInfo.shardId} demorou mais de 20 segundos para responder... \uD83D\uDE22 ~ Irei reiniciar esta shard (e torcer para que não dê problema novamente! \uD83D\uDE47)").complete()
					}

					thread(block = deadShard::shutdownNow)

					val shard = loritta.builder
							.useSharding(shardId, Loritta.config.shards)
							.buildBlocking()

					shard.addEventListener(loritta.updateTimeListener)
					shard.addEventListener(loritta.discordListener)
					shard.addEventListener(loritta.eventLogListener)

					lorittaShards.shards.add(shard)

					guild = loritta.lorittaShards.getGuildById("297732013006389252")
					if (guild != null) {
						val textChannel = guild.getTextChannelById("297732013006389252")
						textChannel.sendMessage("✨ **|** Shard ${shard.shardInfo.shardId} foi reiniciada com sucesso! \uD83D\uDC4F").complete()
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}