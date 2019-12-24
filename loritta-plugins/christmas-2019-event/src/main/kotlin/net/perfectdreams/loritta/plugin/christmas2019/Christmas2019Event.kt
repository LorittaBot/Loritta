package net.perfectdreams.loritta.plugin.christmas2019

import com.mrpowergamerbr.loritta.network.Databases
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import net.perfectdreams.loritta.plugin.christmas2019.commands.Christmas2019Command
import net.perfectdreams.loritta.plugin.christmas2019.listeners.GetChristmasStuffListener
import net.perfectdreams.loritta.plugin.christmas2019.modules.DropChristmasStuffModule
import net.perfectdreams.loritta.tables.Christmas2019Players
import net.perfectdreams.loritta.tables.CollectedChristmas2019Points
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class Christmas2019Event : DiscordPlugin() {
	override fun onEnable() {
		// ===[ NATAL 2019 ]===
		val config = Christmas2019Config()

		registerCommand(Christmas2019Command())
		registerEventListeners(
				GetChristmasStuffListener(config)
		)

		registerMessageReceivedModules(
				DropChristmasStuffModule(config)
		)

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					Christmas2019Players,
					CollectedChristmas2019Points
			)
		}
	}

	override fun onDisable() {
		super.onDisable()
	}

	companion object {
		private val logger = KotlinLogging.logger {}
	}
}