package net.perfectdreams.loritta.commands.vanilla.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.audio.TrackRequest
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class PlayCommand : LorittaCommand(arrayOf("play", "tocar"), CommandCategory.MUSIC) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.audio.play.description"]
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val audioManager = LorittaLauncher.loritta.audioManager
        val channel = context.handle.voiceState?.channel

        if (audioManager == null || channel == null)
            return

        val link = audioManager.connect(channel)
        val musicManager = audioManager.getOrCreateMusicManager(context.discordGuild!!, link) ?: return
        val audioTrack = musicManager.audioManager.resolveTrack(context.args[0])
        musicManager.scheduler.isLooping = false // Remover de loop
        musicManager.scheduler.queue(
                TrackRequest(
                        context.userHandle,
                        audioTrack
                )
        )

        context.reply(
                "Adicionado na fila `${audioTrack.info.title.escapeMentions().stripCodeMarks()}`!",
                "\uD83D\uDCBD"
        )
    }
}