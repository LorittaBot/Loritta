package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.soundbox

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.utils.soundboard.SoundboardAudio

@Serializable
data class PlayAudioClipData(
    val clip: SoundboardAudio
)