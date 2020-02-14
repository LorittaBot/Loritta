package net.perfectdreams.loritta.plugin.funky

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.funky.audio.FunkyManager
import net.perfectdreams.loritta.plugin.funky.commands.*
import net.perfectdreams.loritta.plugin.funky.tables.LavalinkTracks
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class FunkyPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	val funkyManager = FunkyManager(loritta as Loritta, (loritta as Loritta).audioManager!!)

	override fun onEnable() {
		super.onEnable()

		registerCommands(
				LoopCommand.command(loritta, this),
				PauseCommand.command(loritta, this),
				PlayCommand.command(loritta, this),
				PlaylistCommand.command(loritta, this),
				PlayNowCommand.command(loritta, this),
				ResumeCommand.command(loritta, this),
				ShuffleCommand.command(loritta, this),
				SkipCommand.command(loritta, this),
				StopCommand.command(loritta, this),
				VolumeCommand.command(loritta, this)
		)

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					LavalinkTracks
			)
		}
	}

	override fun onDisable() {
		super.onDisable()
	}
}