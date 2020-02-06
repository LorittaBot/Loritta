package net.perfectdreams.loritta.commands.vanilla.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class PauseCommand : LorittaCommand(arrayOf("pause", "pausar"), CommandCategory.MUSIC) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.audio.pause.description"]
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val audioManager = LorittaLauncher.loritta.audioManager ?: return

        val musicManager = audioManager.getMusicManager(context.discordGuild!!) ?: return
        musicManager.scheduler.player.isPaused = true

        context.reply(
                "Música pausada"
        )
    }
}