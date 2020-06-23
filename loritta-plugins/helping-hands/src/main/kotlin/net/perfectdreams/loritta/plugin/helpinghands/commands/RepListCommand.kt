package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.plugin.helpinghands.commands.base.DSLCommandBase
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import com.mrpowergamerbr.loritta.utils.TextUtils.stripCodeMarks

object RepListCommand : DSLCommandBase {
    override fun command(plugin: HelpingHandsPlugin, loritta: LorittaBot) = create(
            loritta,
            listOf("reps", "replist")
    ) {
        description { it["commands.social.repList.description"] }

        examples {
            listOf(
                    "@MrPowerGamerBR",
                    "@Loritta"
            )
        }

        usage {
            arguments {
                argument(ArgumentType.USER) {
                    optional = true
                }
            }
        }

        executes {
            loritta as Loritta

            val context = checkType<DiscordCommandContext>(this)
            val user = context.user(0)?.toJDA() ?: context.user

            val reputations = transaction(Databases.loritta) {
                Reputations.select {
                    Reputations.givenById eq user.idLong or (Reputations.receivedById eq user.idLong)
                }.orderBy(Reputations.receivedAt, SortOrder.DESC)
                        .limit(20)
                        .toMutableList()
            }

            val description = buildString {
                if (reputations.size == 0) {
                    this.append(context.locale["commands.social.repList.noReps"])
                } else {
                    val totalReputationReceived = reputations.filter { it[Reputations.receivedById] == user.idLong }.size;
                    val totalReputationGiven = reputations.filter { it[Reputations.givenById] == user.idLong }.size;
                    this.append(context.locale["commands.social.repList.reputationsTotalDescription", totalReputationReceived, totalReputationGiven])
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

                        val receivedByUser = if (receivedReputation) {
                            lorittaShards.retrieveUserById(reputation[Reputations.givenById])
                        } else {
                            lorittaShards.retrieveUserById(reputation[Reputations.receivedById])
                        }

                        val name = (receivedByUser?.name + "#" + receivedByUser?.discriminator)
                        val content = if (reputation[Reputations.content] != null)
<<<<<<< HEAD
                            "`${reputation[Reputations.content].stripCodeMarks()}`"
=======
                            "`${reputation[Reputations.content]}`"
>>>>>>> a0e6ff5f146b7f0499691dd019def0f5e98f0ea1
                        else
                            null

                        val receivedByLoritta = reputation[Reputations.givenById] == com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.clientId.toLong()
                        if (receivedByLoritta) {
                            str.append(context.locale["commands.social.repList.receivedReputationByLoritta", "`${user.name + "#" + user.discriminator}`"])
                        } else {
                            if (receivedReputation) {
                                if (content == null) {
                                    str.append(context.locale["commands.social.repList.receivedReputation", "`${name}`"])
                                } else {
                                    str.append(context.locale["commands.social.repList.receivedReputationWithContent", "`${name}`", content])
                                }
                            } else {
                                if (content == null) {
                                    str.append(context.locale["commands.social.repList.sentReputation", "`${name}`"])
                                } else {
                                    str.append(context.locale["commands.social.repList.sentReputationWithContent", "`${name}`", content])
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
                                    if (user != context.user)
                                        context.locale["commands.social.repList.otherUserRepList", user.asTag]
                                    else
                                        context.locale["commands.social.repList.title"]
                    )
                    .setColor(Constants.LORITTA_AQUA)
                    .setDescription(description)

            context.sendMessage(context.getUserMention(true), embed.build())
        }
    }
}