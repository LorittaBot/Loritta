package net.perfectdreams.loritta.premium

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.perfectdreams.loritta.premium.listeners.GuildListener
import net.perfectdreams.loritta.premium.network.Databases
import net.perfectdreams.loritta.premium.tables.DonationKeys
import net.perfectdreams.loritta.premium.tables.ServerConfigs
import net.perfectdreams.loritta.premium.utils.Constants
import net.perfectdreams.loritta.premium.utils.config.PremiumConfig
import net.perfectdreams.loritta.premium.utils.extensions.await
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LorittaPremium(val config: PremiumConfig) {
	companion object {
		lateinit var INSTANCE: LorittaPremium
		private val logger = KotlinLogging.logger {}
	}

	val coroutineExecutor = createThreadPool("Coroutine Executor Thread %d")
	val coroutineDispatcher = coroutineExecutor.asCoroutineDispatcher() // Coroutine Dispatcher
	lateinit var jda: JDA

	fun createThreadPool(name: String): ExecutorService {
		return Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())
	}

	fun getActiveDonationKeyForGuild(guild: Guild) = transaction(Databases.loritta) {
		(ServerConfigs innerJoin DonationKeys).slice(ServerConfigs.id, DonationKeys.expiresAt, DonationKeys.value)
				.select {
					(ServerConfigs.id eq guild.idLong) and (ServerConfigs.donationKey eq DonationKeys.id) and (DonationKeys.expiresAt greaterEq System.currentTimeMillis())
				}.firstOrNull()
	}

	fun start() {
		INSTANCE = this
		jda = JDABuilder()
				.setToken(config.discordToken)
				.addEventListeners(GuildListener(this))
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.VOICE_STATE))
				.build()
				.awaitReady()

		GlobalScope.launch {
			while (true) {
				jda.presence.activity = Activity.of(
						Activity.ActivityType.WATCHING,
						"\uD83D\uDCB8 https://loritta.website/donate | Doe para conseguir me ter no seu servidor :3"
				)

				logger.info { "Currently in ${jda.guilds.size} guilds"}
				for (guild in jda.guilds) {
					val activeDonationKey = getActiveDonationKeyForGuild(guild)

					logger.info { "Guild $guild (periodic check) uses key $activeDonationKey"}

					if (guild.idLong in config.doNotKickOut)
						continue

					if (activeDonationKey == null || Constants.PREMIUM_BOT_VALUE >= activeDonationKey[DonationKeys.value]) {
						logger.info { "Leaving guild $guild (periodic check) that uses key $activeDonationKey... sadly they don't meet our fancy requirements!" }
						guild.leave().await()
					}
				}

				delay(60_000)
			}
		}
	}
}