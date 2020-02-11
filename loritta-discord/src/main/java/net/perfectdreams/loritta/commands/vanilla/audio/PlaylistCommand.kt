package net.perfectdreams.loritta.commands.vanilla.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class PlaylistCommand : LorittaCommand(arrayOf("playlist", "fila"), CommandCategory.MUSIC) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.audio.queue.description"]
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val audioManager = LorittaLauncher.loritta.audioManager ?: return

        val musicManager = audioManager.getMusicManager(context.discordGuild!!) ?: return

        val embed = EmbedBuilder()
                .setTitle("Músicas na fila")
                .setColor(Constants.LORITTA_AQUA)
                .setDescription(
                        musicManager.scheduler.queue.toList().joinToString("\n") { it.track.info.title }
                )

        musicManager.scheduler.queue.toList()

        context.sendMessage(embed.build())
    }
}