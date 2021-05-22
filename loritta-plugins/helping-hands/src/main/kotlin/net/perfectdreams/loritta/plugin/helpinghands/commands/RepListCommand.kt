package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.doReactions
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.ZoneId

class RepListCommand(val plugin: HelpingHandsPlugin) : DiscordAbstractCommandBase(
        plugin.loritta,
        listOf("rep list", "reps", "reputations", "reputações", "reputacoes", "reputation list", "reputação list", "reputacao list"),
        CommandCategory.SOCIAL
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
                Reputations.select {
                    Reputations.receivedById eq user.idLong
                }.count()
            }

            val totalReputationGiven = loritta.newSuspendedTransaction {
                Reputations.select {
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
            Reputations.select {
                Reputations.givenById eq user.idLong or (Reputations.receivedById eq user.idLong)
            }.orderBy(Reputations.receivedAt, SortOrder.DESC)
                .limit(ENTRIES_PER_PAGE, page * ENTRIES_PER_PAGE)
                .toList()
        }

        val totalReputationReceived = loritta.newSuspendedTransaction {
            Reputations.select {
                Reputations.receivedById eq user.idLong
            }.count()
        }

        val totalReputationGiven = loritta.newSuspendedTransaction {
            Reputations.select {
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

                    val givenAtTime = Instant.ofEpochMilli(reputation[Reputations.receivedAt]).atZone(ZoneId.systemDefault())
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

                    val receivedByUser = lorittaShards.retrieveUserInfoById(receivedByUserId)

                    val name = ("${receivedByUser?.name}#${receivedByUser?.discriminator} ($receivedByUserId)")
                    val content = reputation[Reputations.content]?.stripCodeMarks()
                        // Strip new lines and replace them with " "
                        ?.replace(Regex("[\\r\\n]"), " ")
                        ?.substringIfNeeded(0..250)

                    val receivedByLoritta = reputation[Reputations.givenById] == com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.clientId.toLong()
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
            if (allowForward && it.reactionEmote.isEmote("⏩")) {
                sendRepListEmbed(
                    context,
                    locale,
                    page + 1,
                    message,
                )
            }
            if (allowBack && it.reactionEmote.isEmote("⏪")) {
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