package net.perfectdreams.loritta.plugin.malcommands.commands

import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.malcommands.MalCommandsPlugin
import net.perfectdreams.loritta.plugin.malcommands.models.AnimeStatus
import net.perfectdreams.loritta.plugin.malcommands.models.AnimeType
import net.perfectdreams.loritta.plugin.malcommands.util.MalConstants.MAL_COLOR
import net.perfectdreams.loritta.plugin.malcommands.util.MalUtils

class MalAnimeCommand(val m: MalCommandsPlugin) : DiscordAbstractCommandBase(m.loritta, listOf("malanime", "anime"), CommandCategory.ANIME) {
    private val LOCALE_PREFIX = "commands.command.malanime"
    private val logger = KotlinLogging.logger { }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")
        localizedExamples("$LOCALE_PREFIX.examples")

        usage {
            argument(ArgumentType.TEXT) {}
        }

        executesDiscord {
            if (args.isEmpty())
                explainAndExit()

            val embed = EmbedBuilder()
            val query = args.joinToString(" ")

            logger.debug { "The anime query is \"$query\"" }

            val animeQuery = MalUtils.queryAnime(query).first() ?: fail(locale["$LOCALE_PREFIX.notfound"])

            val anime = MalUtils.parseAnime(animeQuery)!!

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
                    AnimeType.UNKNOWN -> locale["$LOCALE_PREFIX.unknown"]
                }, true)
                // Anime airing status
                addField(locale["$LOCALE_PREFIX.status.name"], when (anime.info.status) {
                    AnimeStatus.CURRENTLY_AIRING -> locale["$LOCALE_PREFIX.status.airing"]
                    AnimeStatus.NOT_YET_AIRED -> locale["$LOCALE_PREFIX.status.not_yet_aired"]
                    AnimeStatus.FINISHED_AIRING -> locale["$LOCALE_PREFIX.status.finished"]
                    AnimeStatus.UNKNOWN -> locale["$LOCALE_PREFIX.unknown"]
                }, true)
                // "Aired at" status
                addField("\uD83D\uDCC6 " + locale["$LOCALE_PREFIX.status.aired"], anime.info.aired, true)
                // MAL scoring stuff
                addField("⭐ " + locale["$LOCALE_PREFIX.score"], anime.score, true)
                addField("\uD83C\uDF1F " + locale["$LOCALE_PREFIX.rank"], anime.rank, true)
                addField("\uD83E\uDD29 " + locale["$LOCALE_PREFIX.popularity"], anime.popularity, true)
                // Episodes, genres and source info
                addField(locale["$LOCALE_PREFIX.episodes"], anime.info.episodes?.toString()
                        ?: locale["$LOCALE_PREFIX.unknown"], true)
                addField(locale["$LOCALE_PREFIX.genres"], anime.info.genres!!.joinToString(", "), true)
                addField(locale["$LOCALE_PREFIX.source"], anime.info.source, true)
                // Synopsis!
                setDescription(anime.synopsis)
            }
            sendMessage(embed.build())
        }
    }
}
