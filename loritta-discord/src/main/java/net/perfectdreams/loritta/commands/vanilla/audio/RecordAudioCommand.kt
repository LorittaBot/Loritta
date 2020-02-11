package net.perfectdreams.loritta.commands.vanilla.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class RecordAudioCommand : LorittaCommand(arrayOf("record"), CommandCategory.FUN) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.audio.record.description"]
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        if (context.args.firstOrNull() == "stop") {
            context.reply(
                    LoriReply(
                            "Bye bye!"
                    )
            )

            val output = LorittaLauncher.loritta.audioRecorder.stopRecording(context.discordGuild!!)
            context.sendFile(output, "recording.ogg", "fim!")
            return
        }

        context.reply(
                LoriReply(
                        "To gravando, vai ficar supimpa :3"
                )
        )

        LorittaLauncher.loritta.audioRecorder.startRecording(context.discordGuild!!, context.handle.voiceState?.channel!!)
    }
}