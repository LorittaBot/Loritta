package net.perfectdreams.loritta.website.backend.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.views.StaffView
import org.jsoup.Jsoup

/**
 * Tracks user information from last.fm
 *
 * The information is used in the [StaffView] to show the user's "Listening To" and "Most played in the last 7 days" song
 */
class LastFmTracker(val m: LorittaWebsiteBackend) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        try {
            // Clone the current map or create a new one if it isn't present
            val newData = m.lastFmStaffData?.toMutableMap() ?: mutableMapOf()

            StaffView.staffList.groups.flatMap { it.users }
                .forEach {
                    val lastFmSocialNetwork =
                        it.socialNetworks.filterIsInstance<StaffView.Companion.LastFmSocialNetwork>()
                            .firstOrNull()

                    if (lastFmSocialNetwork != null) {
                        try {
                            logger.info { "Querying ${lastFmSocialNetwork.handle} user info..." }

                            val topSongInTheLast7Days = getTopSongInTheLast7Days(lastFmSocialNetwork.handle)
                            val nowListening = getNowListeningSong(lastFmSocialNetwork.handle)

                            newData[lastFmSocialNetwork.handle] = LastFmUserInfo(
                                topSongInTheLast7Days,
                                nowListening
                            )
                        } catch (e: Exception) {
                            logger.warn(e) { "Something went wrong while trying to update ${lastFmSocialNetwork.handle}'s last.fm user status!" }
                        }
                    }
                }

            m.lastFmStaffData = newData
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to update last.fm user statuses!" }
        }
    }

    private fun getTopSongInTheLast7Days(username: String): LastFmSong? {
        val chartlist = Jsoup.connect("https://www.last.fm/user/$username/partial/tracks?tracks_date_preset=LAST_7_DAYS&ajax=1")
            .timeout(120_000) // last.fm is sloooow
            .get()
            .getElementsByClass("chartlist")
            .first() ?: return null

        val name = chartlist.getElementsByClass("chartlist-name")
            .first() ?: return null

        val artist = chartlist.getElementsByClass("chartlist-artist")
            .first() ?: return null

        return LastFmSong(name.text(), artist.text())
    }

    private fun getNowListeningSong(username: String): LastFmSong? {
        val document = Jsoup.connect("https://www.last.fm/user/$username/partial/recenttracks?ajax=1")
            .timeout(120_000) // last.fm is sloooow
            .get()

        val nowScrobbling = document.getElementsByClass("chartlist-row--now-scrobbling")
            .first() ?: return null

        val name = nowScrobbling.getElementsByClass("chartlist-name").text()
        val artist = nowScrobbling.getElementsByClass("chartlist-artist").text()

        return LastFmSong(name, artist)
    }

    data class LastFmUserInfo(
        val topSongInTheLast7Days: LastFmSong?,
        val nowListening: LastFmSong?
    )

    data class LastFmSong(
        val name: String,
        val artist: String
    )
}