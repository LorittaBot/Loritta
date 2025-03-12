package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.doReactions
import net.perfectdreams.loritta.morenitta.utils.extensions.edit
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.stripLinks
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class RepListCommand(val m: LorittaBot) : DiscordAbstractCommandBase(
        m,
        listOf("rep list", "reps", "reputations", "reputações", "reputacoes", "reputation list", "reputação list", "reputacao list"),
        net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL
) {
    companion object {
        private const val ENTRIES_PER_PAGE = 10
        private const val LOCALE_PREFIX = "commands.command.replist"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")
        localizedExamples("$LOCALE_PREFIX.examples")

        usage {
            arguments {
                argument(ArgumentType.USER) {
                    optional = true
                }
            }
        }

        executesDiscord {
            var customPage = if (user(0) != null) {
                args.getOrNull(1)?.toLongOrNull()
            } else {
                args.getOrNull(0)?.toLongOrNull()
            }

            if (customPage != null) {
                customPage -= 1
            }

            if (customPage != null && !RankingGenerator.isValidRankingPage(customPage)) {
                reply(
                    LorittaReply(
                        locale["commands.command.transactions.pageDoesNotExist"],
                        Constants.ERROR
                    )
                )
                return@executesDiscord
            }

            if (customPage == null) customPage = 0

            val totalReputationReceived = loritta.newSuspendedTransaction {
                Reputations.selectAll().where {
                    Reputations.receivedById eq user.idLong
                }.count()
            }

            val totalReputationGiven = loritta.newSuspendedTransaction {
                Reputations.selectAll().where {
                    Reputations.givenById eq user.idLong
                }.count()
            }

            if ((totalReputationGiven + totalReputationReceived) == 0L) {
                reply(
                    LorittaReply(
                        locale["${LOCALE_PREFIX}.unknownReps"]
                    )
                )
                return@executesDiscord
            }

            sendRepListEmbed(
                this,
                locale,
                customPage,
                null
            )
        }
    }

    suspend fun sendRepListEmbed(context: DiscordCommandContext, locale: BaseLocale, item: Long?, currentMessage: Message?) {
        val user = context.user(0)?.handle ?: context.user

        var page = item

        if (page == null) page = 0

        val reputations = loritta.newSuspendedTransaction {
            Reputations.selectAll().where {
                Reputations.givenById eq user.idLong or (Reputations.receivedById eq user.idLong)
            }.orderBy(Reputations.receivedAt, SortOrder.DESC)
                .limit(ENTRIES_PER_PAGE)
                .offset(page * ENTRIES_PER_PAGE)
                .toList()
        }

        val totalReputationReceived = loritta.newSuspendedTransaction {
            Reputations.selectAll().where {
                Reputations.receivedById eq user.idLong
            }.count()
        }

        val totalReputationGiven = loritta.newSuspendedTransaction {
            Reputations.selectAll().where {
                Reputations.givenById eq user.idLong
            }.count()
        }

        val description = buildString {
            if (reputations.size == 0) {
                this.append(locale["$LOCALE_PREFIX.noReps"])
            } else {
                this.append(locale["$LOCALE_PREFIX.reputationsTotalDescription", totalReputationReceived, totalReputationGiven])
                this.append("\n")
                this.append("\n")

                for (reputation in reputations) {
                    // Needed for checking if the string don't bypass 2048 chars limit
                    val str = StringBuilder()

                    val receivedReputation = reputation[Reputations.receivedById] == user.idLong

                    val givenAtTime = Instant.ofEpochMilli(reputation[Reputations.receivedAt]).atZone(Constants.LORITTA_TIMEZONE)
                    val year = givenAtTime.year
                    val month = givenAtTime.monthValue.toString().padStart(2, '0')
                    val day = givenAtTime.dayOfMonth.toString().padStart(2, '0')
                    val hour = givenAtTime.hour.toString().padStart(2, '0')
                    val minute = givenAtTime.minute.toString().padStart(2, '0')
                    str.append("`[$day/$month/$year $hour:$minute]` ")

                    val emoji = if (receivedReputation)
                        "\uD83D\uDCE5"
                    else
                        "\uD83D\uDCE4"
                    str.append(emoji)
                    str.append(" ")

                    val receivedByUserId = if (receivedReputation) {
                        reputation[Reputations.givenById]
                    } else {
                        reputation[Reputations.receivedById]
                    }

                    KotlinLogging.logger {}.info { "RepListCommand#retrieveUserInfoById - UserId: $receivedByUserId" }
                    val receivedByUser = loritta.lorittaShards.retrieveUserInfoById(receivedByUserId)

                    val name = ("${receivedByUser?.name}#${receivedByUser?.discriminator} ($receivedByUserId)")
                    val content = reputation[Reputations.content]?.stripCodeMarks()
                        // Strip new lines and replace them with " "
                        ?.stripLinks()
                        ?.replace(Regex("[\\r\\n]"), " ")
                        ?.substringIfNeeded(0..250)

                    val receivedByLoritta = reputation[Reputations.givenById] == loritta.config.loritta.discord.applicationId.toString().toLong()
                    if (receivedByLoritta) {
                        str.append(locale["$LOCALE_PREFIX.receivedReputationByLoritta", "`${user.name + "#" + user.discriminator}`"])
                    } else {
                        if (receivedReputation) {
                            if (content.isNullOrBlank()) {
                                str.append(locale["$LOCALE_PREFIX.receivedReputation", "`${name}`"])
                            } else {
                                str.append(locale["$LOCALE_PREFIX.receivedReputationWithContent", "`${name}`", "`$content`"])
                            }
                        } else {
                            if (content.isNullOrBlank()) {
                                str.append(locale["$LOCALE_PREFIX.sentReputation", "`${name}`"])
                            } else {
                                str.append(locale["$LOCALE_PREFIX.sentReputationWithContent", "`${name}`", "`$content`"])
                            }
                        }
                    }
                    str.append("\n")

                    // If it's not bypassing the 2048 chars limit, then append
                    if (this.length + str.length <= 2048) {
                        this.append(str)
                    } else {
                        // If else, stop appending strings
                        return@buildString
                    }
                }
            }
        }

        val embed = EmbedBuilder()
            .setTitle(
                "${Emotes.LORI_RICH} " +
                        if (user != user)
                            "${locale["$LOCALE_PREFIX.otherUserRepList", user.asTag]} — ${locale["commands.command.transactions.page"]} ${page + 1}"
                        else
                            "${locale["$LOCALE_PREFIX.title"]} — ${locale["commands.command.transactions.page"]} ${page + 1}"
            )
            .setColor(Constants.LORITTA_AQUA)
            .setDescription(description)

        val message = currentMessage?.edit(context.getUserMention(true), embed.build(), clearReactions = false) ?: context.sendMessage(context.getUserMention(true), embed.build())

        val totalReps = totalReputationGiven + totalReputationReceived

        // We don't want the user to see more than 100 pages of reputation
        val allowForward = totalReps >= (page + 1) * ENTRIES_PER_PAGE && (100 > page)
        val allowBack = page != 0L

        message.onReactionByAuthor(context) {
            if (allowForward && it.emoji.isEmote("⏩")) {
                sendRepListEmbed(
                    context,
                    locale,
                    page + 1,
                    message,
                )
            }
            if (allowBack && it.emoji.isEmote("⏪")) {
                sendRepListEmbed(
                    context,
                    locale,
                    page - 1,
                    message,
                )
            }
        }

        val emotes = mutableListOf<String>()

        if (allowBack)
            emotes.add("⏪")
        if (allowForward)
            emotes.add("⏩")

        message.doReactions(*emotes.toTypedArray())
    }
}