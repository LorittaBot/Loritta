package net.perfectdreams.loritta.plugin.malcommands.commands

import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.malcommands.MalCommandsPlugin
import net.perfectdreams.loritta.plugin.malcommands.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.malcommands.util.AnimeStatus
import net.perfectdreams.loritta.plugin.malcommands.util.AnimeType
import net.perfectdreams.loritta.plugin.malcommands.util.MalConstants.MAL_COLOR
import net.perfectdreams.loritta.plugin.malcommands.util.MalUtils

object MalAnimeCommand : DSLCommandBase {
    private const val LOCALE_PREFIX = "commands.anime.mal.anime"
    private val logger = KotlinLogging.logger { }

    override fun command(loritta: LorittaDiscord, m: MalCommandsPlugin) = create(loritta, listOf("malanime", "anime")) {
        description { it["${LOCALE_PREFIX}.description"] }

        examples {
            listOf(
                    "Nichijou",
                    "Pop Team Epic"
            )
        }
        usage {
            argument(ArgumentType.TEXT) {}
        }

        executesDiscord {
            if (args.isEmpty()) {
                explain()
                return@executesDiscord
            }

            val embed = EmbedBuilder()
            val query = args.joinToString(" ")
            val anime = MalUtils.parseAnimeByQuery(query)
            if (anime != null) {
                logger.debug { "The anime is not null! The anime's score is ${anime.score}!" }
                logger.debug { anime.info.genres!! }
                logger.debug { anime.image }

                val emoji = when (anime.info.type) {
                    AnimeType.MOVIE -> "\uD83C\uDFA5 "
                    else -> "\uD83D\uDCFA "
                }

                embed.apply {
                    setTitle(emoji + anime.info.name, anime.url)
                    setColor(MAL_COLOR)
                    setThumbnail(anime.image)
                    addField(locale["${LOCALE_PREFIX}.type.name"], when (anime.info.type) {
                        AnimeType.TV -> locale["${LOCALE_PREFIX}.type.tv"]
                        AnimeType.SPECIAL -> locale["${LOCALE_PREFIX}.type.special"]
                        AnimeType.OVA -> locale["${LOCALE_PREFIX}.type.ova"]
                        AnimeType.ONA -> locale["${LOCALE_PREFIX}.type.ona"]
                        AnimeType.MOVIE -> locale["${LOCALE_PREFIX}.type.movie"]
                        AnimeType.UNKNOWN -> locale["${LOCALE_PREFIX}.type.unknown"]
                    }, true)
                    addField(locale["${LOCALE_PREFIX}.status.name"], when (anime.info.status) {
                        AnimeStatus.CURRENTLY_AIRING -> locale["${LOCALE_PREFIX}.status.airing"]
                        AnimeStatus.NOT_YET_AIRED -> locale["${LOCALE_PREFIX}.status.not_yet_aired"]
                        AnimeStatus.FINISHED_AIRING -> locale["${LOCALE_PREFIX}.status.finished"]
                        AnimeStatus.UNKNOWN -> locale["${LOCALE_PREFIX}.status.unknown"]
                    }, true)
                    addField("\uD83D\uDCC6 " + locale["${LOCALE_PREFIX}.status.aired"], anime.info.aired, true)
                    addField("⭐ " + locale["${LOCALE_PREFIX}.score"], anime.score, true)
                    addField("\uD83C\uDF1F " + locale["${LOCALE_PREFIX}.rank"], anime.rank, true)
                    addField("\uD83E\uDD29 " + locale["${LOCALE_PREFIX}.popularity"], anime.popularity, true)
                    addField(locale["${LOCALE_PREFIX}.episodes"], anime.info.episodes.toString(), true)
                    addField(locale["${LOCALE_PREFIX}.genres"], anime.info.genres!!.joinToString(", "), true)
                    addField(locale["${LOCALE_PREFIX}.source"], anime.info.source, true)
                    setDescription(anime.synopsis)
                }
                sendMessage(embed.build())
            } else {
                logger.debug { "The anime is null, the query was \"${query}\"" }
                reply(
                        LorittaReply(
                                locale["${LOCALE_PREFIX}.notfound"],
                                Constants.ERROR
                        )
                )
            }
        }
    }
}
