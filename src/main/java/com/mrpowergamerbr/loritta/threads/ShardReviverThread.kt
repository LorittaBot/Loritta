package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.listeners.MusicMessageListener
import com.mrpowergamerbr.loritta.listeners.UpdateTimeListener
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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
		for (shard in lorittaShards.shards) {
			val lastUpdate = lorittaShards.lastJdaEventTime.getOrDefault(shard, System.currentTimeMillis())

			val seconds = (System.currentTimeMillis() - lastUpdate) / 1000

			if (seconds >= 3) {
				println("[!] Shard ${shard.shardInfo.shardId} não recebeu update a mais de 3s! ~  ${seconds}s")
			}
		}

		val deadShards = lorittaShards.shards.filter {
			val lastUpdate = lorittaShards.lastJdaEventTime.getOrDefault(it, System.currentTimeMillis())

			System.currentTimeMillis() - lastUpdate > 10000
		}

		if (deadShards.isNotEmpty()) {
			val okHttpBuilder = OkHttpClient.Builder()
					.connectTimeout(60, TimeUnit.SECONDS)
					.readTimeout(60, TimeUnit.SECONDS)
					.writeTimeout(60, TimeUnit.SECONDS)

			val discordListener = DiscordListener(loritta); // Vamos usar a mesma instância para todas as shards
			val eventLogListener = EventLogListener(loritta); // Vamos usar a mesma instância para todas as shards
			val updateTimeListener = UpdateTimeListener(loritta);
			val messageListener = MusicMessageListener(loritta)

			for (deadShard in deadShards) {
				println("Reiniciando shard ${deadShard.shardInfo.shardId}")
				val guild = loritta.lorittaShards.getGuildById("297732013006389252")
				if (guild != null) {
					val textChannel = guild.getTextChannelById("297732013006389252")
					textChannel.sendMessage("Shard ${deadShard.shardInfo.shardId} demorou mais de 10 segundos para responder... \uD83D\uDE22 ~ Irei reiniciar esta shard (e torcer para que não dê problema novamente! \uD83D\uDE47)").complete()
				}
				val shardId = deadShard.shardInfo.shardId

				lorittaShards.shards.remove(deadShard)
				lorittaShards.lastJdaEventTime.remove(deadShard)

				deadShard.shutdownNow()

				val shard = JDABuilder(AccountType.BOT)
						.useSharding(shardId, Loritta.config.shards)
						.setToken(Loritta.config.clientToken)
						.setHttpClientBuilder(okHttpBuilder)
						.setCorePoolSize(8)
						.buildBlocking()

				if (!loritta.isMusicOnly) {
					shard.addEventListener(updateTimeListener)
					shard.addEventListener(discordListener)
					shard.addEventListener(eventLogListener)
				} else {
					shard.addEventListener(updateTimeListener)
					shard.addEventListener(messageListener)
				}
			}
		}
	}
}