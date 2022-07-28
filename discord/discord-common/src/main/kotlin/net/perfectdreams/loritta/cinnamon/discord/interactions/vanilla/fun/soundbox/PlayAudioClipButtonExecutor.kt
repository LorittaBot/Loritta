package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.soundbox

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.SoundboardAudioRequest
import java.util.*

class PlayAudioClipButtonExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.PLAY_AUDIO_CLIP_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        if (context !is GuildComponentContext)
            return

        context.deferUpdateMessage()

        when (val voiceStateResult = loritta.validateVoiceState(context.guildId, user.id)) {
            is LorittaCinnamon.AlreadyPlayingInAnotherChannel -> context.failEphemerally {
                // We are already playing in another channel!
                content = "Eu já estou tocando áudio em outro canal! <#${voiceStateResult.lorittaConnectedVoiceChannel}>"
            }
            is LorittaCinnamon.LorittaDoesntHavePermissionToTalkOnChannel -> context.failEphemerally {
                // Looks like we can't...
                content = "Desculpe, mas eu não tenho permissão para falar no canal <#${voiceStateResult.userConnectedVoiceChannel}>!"
            }
            LorittaCinnamon.UserNotConnectedToAVoiceChannel -> context.failEphemerally {
                // Not in a voice channel
                content = "Você precisa estar conectado em um canal de voz!"
            }
            LorittaCinnamon.VoiceStateTimeout -> context.failEphemerally {
                // Looks like something went wrong! Took too long to get if I'm in a voice channel or not
                content = "Deu ruim!"
            }
            is LorittaCinnamon.VoiceStateValidationData -> {
                // Success! Let's notify the user...
                val audioClipData = context.decodeDataFromComponentOrFromDatabase<PlayAudioClipData>()

                loritta.services.notify(
                    SoundboardAudioRequest(
                        UUID.randomUUID().toString(),
                        context.guildId.toLong(),
                        voiceStateResult.userConnectedVoiceChannel.toLong(),
                        audioClipData.clip
                    )
                )
            }
        }
    }
}