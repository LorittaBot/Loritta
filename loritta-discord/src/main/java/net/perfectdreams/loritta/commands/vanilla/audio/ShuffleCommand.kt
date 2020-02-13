package net.perfectdreams.loritta.commands.vanilla.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class ShuffleCommand : LorittaCommand(arrayOf("shuffle"), CommandCategory.MUSIC) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.audio.shuffle.description"]
    }

    override val lorittaPermissions = listOf(LorittaPermission.DJ)

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val audioManager = LorittaLauncher.loritta.audioManager ?: return

        val musicManager = audioManager.getMusicManager(context.discordGuild!!) ?: return

        // Limpar lista de qualquer música que tenha
        val shuffledQueue = musicManager.scheduler.queue.toList()
                .shuffled()

        musicManager.scheduler.queue.clear()
        musicManager.scheduler.queue.addAll(shuffledQueue)

        context.reply(
                "Playlist foi bagunçada!"
        )
    }
}