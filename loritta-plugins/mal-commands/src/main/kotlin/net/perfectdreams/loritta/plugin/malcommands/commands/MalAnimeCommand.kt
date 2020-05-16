package net.perfectdreams.loritta.plugin.malcommands.commands

import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.malcommands.MalCommandsPlugin
import net.perfectdreams.loritta.plugin.malcommands.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.malcommands.util.MalUtils
import java.awt.Color

object MalAnimeCommand: DSLCommandBase {
    private const val LOCALE_PREFIX = "commands.anime.mal.anime"
    private val logger = KotlinLogging.logger {  }
    private val MAL_COLOR = Color(46,81,162)

    override fun command(loritta: LorittaDiscord, m: MalCommandsPlugin) = create(loritta, listOf("malanime")) {
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
            val anime = MalUtils.parseAnimeByQuery(args.joinToString(" "))
            if (anime != null) {
                logger.debug { "O anime não é nulo! Vamos fazer uma embed! A avaliação do anime é ${anime.score}" }
                logger.debug { anime.image }

                embed.apply {
                    setTitle(anime.info.name, anime.url)
                    setColor(MAL_COLOR)
                    setThumbnail(anime.image)
                    addField(locale["${LOCALE_PREFIX}.score"], anime.score, true)
                    addField(locale["${LOCALE_PREFIX}.episodes"], anime.info.episodes.toString(), true)
                    setDescription(anime.synopsis)
                }
                sendMessage(embed.build())
            } else {
                logger.debug { "Anime nulo" }
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