package net.perfectdreams.loritta.morenitta.website.views.httpapidocs

import kotlinx.html.*
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.utils.TimeUtil
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.CurrentSong
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.PostLoriSongListeningRoute.Companion.lorittaCurrentlyPlayingSong
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

fun DIV.mainframeTerminal(
    title: String,
    content: DIV.() -> (Unit)
) {
    div(classes = "mainframe-terminal-wrapper") {
        div(classes = "mainframe-terminal-title") {
            text(title)
        }

        div(classes = "mainframe-terminal") {
            content.invoke(this)
        }
    }
}

fun DIV.mainframeTerminalLorifetch(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    userIdentification: LorittaJsonWebSession.UserIdentification?,
    title: String,
    terminalContentId: String?,
    guildCount: Int,
    executedCommands: Int,
    uniqueUsersExecutedCommands: Int,
    currentSong: CurrentSong
) {
    mainframeTerminal(title) {
        div {
            if (terminalContentId != null)
                id = terminalContentId

            div {
                span(classes = "term-green") {
                    text("${userIdentification?.username ?: "wumpus"}@loritta:~# ")
                }
                text("lorifetch")
            }

            div {
                style = "display: flex;\n" +
                        "  flex-direction: row;\n" +
                        "  gap: 3em;"

                div {
                    img(src = "https://stuff.loritta.website/lori-pixel.png") {
                        draggable = Draggable.htmlFalse
                        style = "height: 11lh;"
                    }
                }

                div {
                    div(classes = "term-blue") {
                        text("~ Loritta Morenitta ~")
                    }
                    div {
                        text("─────────────────────")
                    }

                    val buildNumber = System.getenv("BUILD_ID")
                    val commitHash = System.getenv("COMMIT_HASH")

                    div {
                        span(classes = "term-orange") {
                            text("Build: ")
                        }

                        if (buildNumber != null && commitHash != null) {
                            text("#${buildNumber} (${commitHash.take(7)})")
                        } else {
                            text("Unknown")
                        }
                    }

                    div {
                        span(classes = "term-orange") {
                            text("Java Version: ")
                        }

                        text(System.getProperty("java.version"))
                    }

                    div {
                        span(classes = "term-orange") {
                            text("Kotlin Version: ")
                        }

                        text(KotlinVersion.CURRENT.toString())
                    }

                    div {
                        span(classes = "term-orange") {
                            text("JDA Version: ")
                        }

                        text(JDAInfo.VERSION.toString())
                    }

                    div {
                        span(classes = "term-orange") {
                            text("Guilds: ")
                        }

                        text(formatNumber(guildCount))
                    }

                    div {
                        span(classes = "term-orange") {
                            text("Executed Commands (24h): ")
                        }

                        text(formatNumber(executedCommands))
                    }

                    div {
                        span(classes = "term-orange") {
                            text("Unique Users (24h): ")
                        }

                        text(formatNumber(uniqueUsersExecutedCommands))
                    }

                    div {
                        span(classes = "term-orange") {
                            text("Uptime: ")
                        }

                        text(
                            DateUtils.formatDateDiff(
                                loritta.languageManager.getI18nContextById("en"), // Yes, we want the english version of this!
                                TimeUtil.getTimeCreated(loritta.config.loritta.discord.applicationId.value.toLong()).toInstant().toEpochMilli(),
                                System.currentTimeMillis(),
                                maxParts = 3
                            )
                        )
                    }

                    div {
                        span(classes = "term-orange") {
                            text("Listening to: ")
                        }

                        span {
                            attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/developers/docs/lori-listening-song"
                            attributes["hx-trigger"] = "every 5s"
                            attributes["hx-target"] = ".lori-currently-listening"

                            span(classes = "lori-currently-listening") {
                                lorittaCurrentlyPlayingSong(currentSong)
                            }
                        }

                        text(" ")

                        span(classes = "term-orange") {
                            text(" ♫")
                        }
                    }

                    /* div {
                        text(Duration.ofSeconds(playlistInfo.songs.sumOf { it.durationInSeconds }).toString())
                    }

                    div {
                        text(playlistInfo.songs.size.toString())
                    } */

                    /* div {
                        a(href = "http://www.youtube.com/watch_videos?video_ids=${playlistInfo.songs.joinToString(",") { it.youtubeId }}") {
                            text("Playlist")
                        }
                    } */
                }
            }
        }
    }
}

private fun formatNumber(number: Int): String {
    return number.toString().reversed().chunked(3).joinToString("_").reversed()
}