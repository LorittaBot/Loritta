package net.perfectdreams.loritta.morenitta.website.views.httpapidocs

import kotlinx.html.*
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.utils.TimeUtil
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.CurrentSong
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import java.time.Duration

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
                    attributes["hx-ext"] = "sse"
                    attributes["hx-swap"] = "innerHTML"
                    attributes["sse-connect"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/developers/docs/lorifetch-stats"
                    attributes["sse-swap"] = "message"

                    mainframeTerminalLorifetchStats(

                        loritta,
                        i18nContext,
                        guildCount,
                        executedCommands,
                        uniqueUsersExecutedCommands,
                        currentSong
                    )
                }
            }
        }
    }
}

fun FlowContent.mainframeTerminalLorifetchStats(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    guildCount: Int,
    executedCommands: Int,
    uniqueUsersExecutedCommands: Int,
    currentSong: CurrentSong
) {
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

        span(classes = "lori-currently-listening") {
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

private fun formatNumber(number: Int): String {
    return number.toString().reversed().chunked(3).joinToString("_").reversed()
}