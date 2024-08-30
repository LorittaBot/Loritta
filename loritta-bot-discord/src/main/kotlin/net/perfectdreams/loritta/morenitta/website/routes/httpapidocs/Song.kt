package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.DurationUnit

// Thanks ChatGPT
@Serializable
data class Song(val title: String, val duration: Duration, val youtubeId: String) {
    val durationInSeconds
        get() = duration.toLong(DurationUnit.SECONDS)
}

data class CurrentSong(
    val song: Song,
    val elapsedSeconds: Long
)

fun findCurrentSong(playlist: List<Song>, startTime: Long, timestamp: Long): CurrentSong {
    val totalDuration = playlist.sumOf { it.durationInSeconds }

    // Calculate the time offset since the start time
    val timeOffset = (timestamp - startTime)

    // Find the effective time within the playlist loop
    val effectiveTime = timeOffset % totalDuration

    // Identify the song being played
    var accumulatedTime = 0L
    for (song in playlist) {
        accumulatedTime += song.durationInSeconds
        if (effectiveTime < accumulatedTime) {
            val elapsedTime = (effectiveTime - accumulatedTime) + song.durationInSeconds
            return CurrentSong(song, elapsedTime)
        }
    }
    throw IllegalStateException("Should never reach here if playlist is non-empty")
}