package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.SoundboardAudioRequest
import java.util.*

class PlayAudioClipButtonExecutor(val m: LorittaCinnamon) : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.PLAY_AUDIO_CLIP_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        if (context !is GuildComponentContext)
            return

        context.deferUpdateMessage()

        when (val voiceStateResult = m.validateVoiceState(context.guildId, user.id)) {
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
                val audioClipData = context.decodeDataFromComponentOrFromDatabase<PlayAudioClipData>(data)

                m.services.notify(
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