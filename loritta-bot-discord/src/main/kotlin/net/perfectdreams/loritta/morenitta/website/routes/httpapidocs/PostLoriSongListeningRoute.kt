package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import java.io.File
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.random.Random

class PostLoriSongListeningRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/developers/docs/lori-listening-song") {
    override val isMainClusterOnlyRoute = true

    override suspend fun onLocalizedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext
    ) {
        val playlistInfo = Yaml.default.decodeFromStream<SongPlaylist>(File(loritta.config.loritta.folders.content, "playlist.yml").inputStream())
        val shuffledPlaylistSongs = playlistInfo.songs.shuffled(Random(0))

        val nowAsZST = ZonedDateTime.now(
            ZoneId.of("America/Sao_Paulo")
        )

        val startTime = playlistInfo.startedPlayingAt.epochSeconds // When the playlist started playing
        val timestamp = nowAsZST.toEpochSecond() // The timestamp we want to check

        val currentSong = findCurrentSong(shuffledPlaylistSongs, startTime, timestamp)

        call.respondHtml(
            createHTML()
                .body {
                    lorittaCurrentlyPlayingSong(currentSong)
                }
        )
    }

    companion object {
        fun FlowOrPhrasingContent.lorittaCurrentlyPlayingSong(currentSong: CurrentSong) {
            a(href = "https://youtu.be/${currentSong.song.youtubeId}?t=${currentSong.elapsedSeconds}", target = "_blank") {
                style = "color: white;"
                text(currentSong.song.title)
            }

            text(" ")
            span(classes = "term-orange") {
                style = "color: #feaae4;"
                val elapsed = Duration.ofSeconds(currentSong.elapsedSeconds)
                val total = Duration.ofSeconds(currentSong.song.durationInSeconds)
                text("(${elapsed.toMinutesPart().toString().padStart(2, '0')}:${elapsed.toSecondsPart().toString().padStart(2, '0')} / ${total.toMinutesPart().toString().padStart(2, '0')}:${total.toSecondsPart().toString().padStart(2, '0')})")
            }
        }
    }
}