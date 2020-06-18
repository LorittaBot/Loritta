package net.perfectdreams.loritta.plugin.malcommands.commands

import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.malcommands.MalCommandsPlugin
import net.perfectdreams.loritta.plugin.malcommands.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.malcommands.commands.models.AnimeStatus
import net.perfectdreams.loritta.plugin.malcommands.commands.models.AnimeType
import net.perfectdreams.loritta.plugin.malcommands.exceptions.MalException
import net.perfectdreams.loritta.plugin.malcommands.exceptions.MalSearchException
import net.perfectdreams.loritta.plugin.malcommands.util.MalConstants.MAL_COLOR
import net.perfectdreams.loritta.plugin.malcommands.util.MalUtils

object MalAnimeCommand : DSLCommandBase {
    private const val LOCALE_PREFIX = "commands.anime.mal.anime"
    private val logger = KotlinLogging.logger { }

    override fun command(loritta: LorittaDiscord, m: MalCommandsPlugin) = create(loritta, listOf("malanime", "anime")) {
        description { it["$LOCALE_PREFIX.description"] }

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
            logger.debug { "The anime query is \"$query\"" }

            try {
                val anime = MalUtils.parseAnimeByQuery(query)

                if (anime == null) {
                    logger.debug { "Failed to query the anime!" }
                    reply(
                            LorittaReply(
                                    locale["$LOCALE_PREFIX.notfound"],
                                    Constants.ERROR
                            )
                    )
                    return@executesDiscord
                } else {
                    // for debugging reasons, there's a "else" block
                    logger.debug { "The anime is not null, that's good." }
                }

                logger.debug { anime.toString() }

                val emoji = when (anime.info.type) {
                    AnimeType.MOVIE -> "\uD83C\uDFA5 "
                    else -> "\uD83D\uDCFA "
                }

                embed.apply {
                    setTitle(emoji + anime.info.name, anime.url)
                    setColor(MAL_COLOR)
                    setThumbnail(anime.image)
                    // Anime type (TV, special, OVA, etc)
                    addField(locale["$LOCALE_PREFIX.type.name"], when (anime.info.type) {
                        AnimeType.TV -> locale["$LOCALE_PREFIX.type.tv"]
                        AnimeType.SPECIAL -> locale["$LOCALE_PREFIX.type.special"]
                        AnimeType.OVA -> locale["$LOCALE_PREFIX.type.ova"]
                        AnimeType.ONA -> locale["$LOCALE_PREFIX.type.ona"]
                        AnimeType.MOVIE -> locale["$LOCALE_PREFIX.type.movie"]
                        AnimeType.UNKNOWN -> locale["$LOCALE_PREFIX.type.unknown"]
                    }, true)
                    // Anime airing status
                    addField(locale["$LOCALE_PREFIX.status.name"], when (anime.info.status) {
                        AnimeStatus.CURRENTLY_AIRING -> locale["$LOCALE_PREFIX.status.airing"]
                        AnimeStatus.NOT_YET_AIRED -> locale["$LOCALE_PREFIX.status.not_yet_aired"]
                        AnimeStatus.FINISHED_AIRING -> locale["$LOCALE_PREFIX.status.finished"]
                        AnimeStatus.UNKNOWN -> locale["$LOCALE_PREFIX.status.unknown"]
                    }, true)
                    // "Aired at" status
                    addField("\uD83D\uDCC6 " + locale["$LOCALE_PREFIX.status.aired"], anime.info.aired, true)
                    // MAL scoring stuff
                    addField("⭐ " + locale["$LOCALE_PREFIX.score"], anime.score, true)
                    addField("\uD83C\uDF1F " + locale["$LOCALE_PREFIX.rank"], anime.rank, true)
                    addField("\uD83E\uDD29 " + locale["$LOCALE_PREFIX.popularity"], anime.popularity, true)
                    // Episodes, genres and source info
                    addField(locale["$LOCALE_PREFIX.episodes"], anime.info.episodes?.toString() ?: locale["$LOCALE_PREFIX.unknown"], true)
                    addField(locale["$LOCALE_PREFIX.genres"], anime.info.genres!!.joinToString(", "), true)
                    addField(locale["$LOCALE_PREFIX.source"], anime.info.source, true)
                    // Synopsis!
                    setDescription(anime.synopsis)
                }
                sendMessage(embed.build())
            } catch(e: Exception) {
                logger.debug { "Something gone wrong when querying an anime from MAL!" }
                logger.debug { e }
                reply(
                        LorittaReply(
                                locale["$LOCALE_PREFIX.notfound"],
                                Constants.ERROR
                        )
                )
            }
        }
    }
}
