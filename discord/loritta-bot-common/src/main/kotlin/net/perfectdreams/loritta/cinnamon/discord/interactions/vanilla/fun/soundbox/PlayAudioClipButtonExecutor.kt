package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.soundbox

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnection
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnectionManager

class PlayAudioClipButtonExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.PLAY_AUDIO_CLIP_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        if (context !is GuildComponentContext)
            return

        context.deferUpdateMessage()

        when (val voiceStateResult = loritta.voiceConnectionsManager.validateVoiceState(context.guildId, user.id)) {
            is LorittaVoiceConnectionManager.VoiceStateValidationResult.AlreadyPlayingInAnotherChannel -> context.failEphemerally {
                // We are already playing in another channel!
                content = "Eu já estou tocando áudio em outro canal! <#${voiceStateResult.lorittaConnectedVoiceChannel}>"
            }
            is LorittaVoiceConnectionManager.VoiceStateValidationResult.LorittaDoesntHavePermissionToTalkOnChannel -> context.failEphemerally {
                // Looks like we can't...
                content = "Desculpe, mas eu não tenho permissão para falar no canal <#${voiceStateResult.userConnectedVoiceChannel}>!"
            }
            LorittaVoiceConnectionManager.VoiceStateValidationResult.UserNotConnectedToAVoiceChannel -> context.failEphemerally {
                // Not in a voice channel
                content = "Você precisa estar conectado em um canal de voz!"
            }
            is LorittaVoiceConnectionManager.VoiceStateValidationResult.VoiceStateValidationData -> {
                // Success! Let's notify the user...
                val audioClipData = context.decodeDataFromComponentOrFromDatabase<PlayAudioClipData>()

                val voiceConnection = loritta.voiceConnectionsManager.getOrCreateVoiceConnection(context.guildId, voiceStateResult.userConnectedVoiceChannel)

                val opusFrames = loritta.soundboard.getAudioClip(audioClipData.clip)

                voiceConnection.queue(
                    LorittaVoiceConnection.AudioClipInfo(
                        opusFrames,
                        voiceStateResult.userConnectedVoiceChannel
                    )
                )
            }
        }
    }
}