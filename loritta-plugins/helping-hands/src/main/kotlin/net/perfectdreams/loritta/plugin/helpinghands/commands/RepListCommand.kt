package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.extensions.toJDA
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
    override fun command() = create {
        localizedDescription("commands.social.repList.description")

        examples {
            + "@MrPowerGamerBR"
            + "@Loritta"
        }

        usage {
            arguments {
                argument(ArgumentType.USER) {
                    optional = true
                }
            }
        }

        executesDiscord {
            val user = user(0)?.toJDA() ?: user

            val reputations = loritta.newSuspendedTransaction {
                Reputations.select {
                    Reputations.givenById eq user.idLong or (Reputations.receivedById eq user.idLong)
                }.orderBy(Reputations.receivedAt, SortOrder.DESC)
                        .limit(50) // We limit according to the message max size later on
                        .toMutableList()
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
                    this.append(locale["commands.social.repList.noReps"])
                } else {
                    this.append(locale["commands.social.repList.reputationsTotalDescription", totalReputationReceived, totalReputationGiven])
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
                                ?.substringIfNeeded(0..500)

                        val receivedByLoritta = reputation[Reputations.givenById] == com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.clientId.toLong()
                        if (receivedByLoritta) {
                            str.append(locale["commands.social.repList.receivedReputationByLoritta", "`${user.name + "#" + user.discriminator}`"])
                        } else {
                            if (receivedReputation) {
                                if (content.isNullOrBlank()) {
                                    str.append(locale["commands.social.repList.receivedReputation", "`${name}`"])
                                } else {
                                    str.append(locale["commands.social.repList.receivedReputationWithContent", "`${name}`", "`$content`"])
                                }
                            } else {
                                if (content.isNullOrBlank()) {
                                    str.append(locale["commands.social.repList.sentReputation", "`${name}`"])
                                } else {
                                    str.append(locale["commands.social.repList.sentReputationWithContent", "`${name}`", "`$content`"])
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
                                        locale["commands.social.repList.otherUserRepList", user.asTag]
                                    else
                                        locale["commands.social.repList.title"]
                    )
                    .setColor(Constants.LORITTA_AQUA)
                    .setDescription(description)

            sendMessage(getUserMention(true), embed.build())
        }
    }
}