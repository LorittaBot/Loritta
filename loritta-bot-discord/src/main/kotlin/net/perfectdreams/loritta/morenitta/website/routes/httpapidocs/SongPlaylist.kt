package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SongPlaylist(
    val startedPlayingAt: Instant,
    val songs: List<Song>,
)