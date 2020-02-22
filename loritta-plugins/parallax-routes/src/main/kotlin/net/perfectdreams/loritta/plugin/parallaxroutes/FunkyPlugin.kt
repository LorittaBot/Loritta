package net.perfectdreams.loritta.plugin.funky

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.funky.audio.FunkyManager
import net.perfectdreams.loritta.plugin.funky.commands.*
import net.perfectdreams.loritta.plugin.funky.routes.ConfigureMusicRoute
import net.perfectdreams.loritta.plugin.funky.tables.LavalinkTracks
import net.perfectdreams.loritta.plugin.funky.tables.MusicConfigs
import net.perfectdreams.loritta.plugin.funky.transformers.MusicConfigTransformer
import net.perfectdreams.loritta.plugin.funky.transformers.VoiceChannelsTransformer
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class FunkyPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val funkyManager = FunkyManager(loritta as Loritta, loritta.audioManager!!)

	override fun onEnable() {
		super.onEnable()

		loritta as Loritta

		registerCommands(
				LoopCommand.command(loritta, this),
				NowPlayingCommand.command(loritta, this),
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

		configTransformers.add(MusicConfigTransformer)
		configTransformers.add(VoiceChannelsTransformer)
		routes.add(ConfigureMusicRoute(loritta))

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					ServerConfigs,
					LavalinkTracks,
					MusicConfigs
			)
		}
	}

	override fun onDisable() {
		super.onDisable()
	}
}