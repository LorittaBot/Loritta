package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.SoundboardAudio

@Serializable
data class PlayAudioClipData(
    val clip: SoundboardAudio
)