package net.perfectdreams.loritta.plugin.funky.commands.base

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.CommandException
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommand
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandBuilder
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.audio.FunkyManager
import net.perfectdreams.loritta.plugin.funky.dao.MusicConfig
import net.perfectdreams.loritta.plugin.funky.tables.musicConfig
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.transactions.transaction

interface DSLCommandBase {
	fun command(loritta: LorittaBot, m: FunkyPlugin): Command<CommandContext>

	fun create(loritta: LorittaBot, labels: List<String>, builder: DiscordCommandBuilder.() -> (Unit)): DiscordCommand {
		return discordCommand(
				loritta as LorittaDiscord,
				this::class.simpleName!!,
				labels,
				CommandCategory.MUSIC
		) {
			canUseInPrivateChannel = false
			
			builder.invoke(this)
		}
	}

	fun ServerConfig.retrieveMusicConfig(): MusicConfig? {
		val serverConfig = this
		return transaction(Databases.loritta) { serverConfig.musicConfig }
	}

	fun DiscordCommandContext.checkMusicPremium() {
		val donationKeysValue = serverConfig.getActiveDonationKeysValue()

		if (19.99 > donationKeysValue)
			throw CommandException(
					"Apenas Premium!",
					Emotes.LORI_RICH.toString()
			)
	}

	fun DiscordCommandContext.checkIfMusicIsPlaying(funkyManager: FunkyManager) = funkyManager.getMusicManager(guild) ?: throw CommandException(
			"Atualmente não tem nenhuma música tocando... Que tal tocar uma com `+play`?",
			Emotes.LORI_CRYING.toString()
	)
}