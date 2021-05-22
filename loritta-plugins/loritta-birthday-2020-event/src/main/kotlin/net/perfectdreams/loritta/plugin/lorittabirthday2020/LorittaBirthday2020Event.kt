package net.perfectdreams.loritta.plugin.lorittabirthday2020

import com.mrpowergamerbr.loritta.network.Databases
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.christmas2019.profile.badges.ChristmasBadge
import net.perfectdreams.loritta.plugin.lorittabirthday2020.badges.GabrielaBadge
import net.perfectdreams.loritta.plugin.lorittabirthday2020.badges.PantufaBadge
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Drops
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.CollectedBirthday2020Points
import net.perfectdreams.loritta.profile.Badge
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaBirthday2020Event(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
	val badges = mutableListOf<Badge>()

	override fun onEnable() {
		loritta as LorittaDiscord
		// this.routes.add(ReceiveStatsRoute(this, loritta))
		// this.routes.add(SyncPointsRoute(this, loritta))
		// this.routes.add(GetCurrentTeamRoute(this, loritta))
		// this.routes.add(PostJoinTeamRoute(this, loritta))

		// this.addMessageReceivedModule(DropBirthdayStuffModule())
		// this.addEventListener(GetBirthdayStuffListener(this))

		// loriToolsExecutors += GiveBirthdayRewardsExecutor
		// loriToolsExecutors += GiveBirthdayKeysExecutor
		// loriToolsExecutors += GiveSafeBirthdayRewardsExecutor
		badges.add(PantufaBadge())
		badges.add(GabrielaBadge())
		badges.add(ChristmasBadge())

		badges.forEach {
			loritta.profileDesignManager.registerBadge(it)
		}

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					Birthday2020Players,
					Birthday2020Drops,
					CollectedBirthday2020Points
			)
		}

		/* launch {
			while (true) {
				LorittaBirthday2020.openChannels.entries.forEach { (idLong, channel) ->
					try {
						channel.send(
								jsonObject(
										"type" to "ping",
										"time" to System.currentTimeMillis()
								)
						)
					} catch (e: Throwable) {
						LorittaBirthday2020.openChannels.remove(idLong, channel)
						channel.close()
					}
				}
				delay(5_000)
			}
		} */
	}

	override fun onDisable() {
		super.onDisable()
		loritta as LorittaDiscord

		badges.forEach {
			loritta.profileDesignManager.unregisterBadge(it)
		}
	}

	companion object {
		private val logger = KotlinLogging.logger {}
	}
}