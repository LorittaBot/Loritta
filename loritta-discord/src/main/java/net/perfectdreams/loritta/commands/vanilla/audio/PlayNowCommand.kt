package net.perfectdreams.loritta.commands.vanilla.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class PlayNowCommand : LorittaCommand(arrayOf("playnow", "tocaragora"), CommandCategory.MUSIC) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.audio.playnow.description"]
    }

    override val lorittaPermissions = listOf(LorittaPermission.DJ)

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
        musicManager.scheduler.player.playTrack(audioTrack)

        context.reply(
                "Adicionado na fila *forçadamente* `${audioTrack.info.title.escapeMentions().stripCodeMarks()}`!",
                "\uD83D\uDCBD"
        )
    }
}