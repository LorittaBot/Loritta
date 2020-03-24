package net.perfectdreams.loritta.plugin.lorittabirthday2020

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.network.Databases
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.lorittabirthday2020.listeners.GetBirthdayStuffListener
import net.perfectdreams.loritta.plugin.lorittabirthday2020.modules.DropBirthdayStuffModule
import net.perfectdreams.loritta.plugin.lorittabirthday2020.routes.*
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Drops
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.CollectedBirthday2020Points
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaBirthday2020Event(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	override fun onEnable() {
		loritta as LorittaDiscord
		this.routes.add(ChooseTeamRoute(loritta))
		this.routes.add(ReceiveStatsRoute(this, loritta))
		this.routes.add(SyncPointsRoute(this, loritta))
		this.routes.add(ViewCurrentStatsRoute(loritta))
		this.routes.add(GetCurrentTeamRoute(this, loritta))
		this.routes.add(PostJoinTeamRoute(this, loritta))

		this.addMessageReceivedModule(DropBirthdayStuffModule())
		this.addEventListener(GetBirthdayStuffListener(this))

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					Birthday2020Players,
					Birthday2020Drops,
					CollectedBirthday2020Points
			)
		}

		launch {
			while (true) {
				LorittaBirthday2020.openChannels.entries.forEach { (idLong, channel) ->
					try {
						logger.info { "Sending a empty json to $idLong" }
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
		}
	}

	override fun onDisable() {
		super.onDisable()
	}

	companion object {
		private val logger = KotlinLogging.logger {}
	}
}