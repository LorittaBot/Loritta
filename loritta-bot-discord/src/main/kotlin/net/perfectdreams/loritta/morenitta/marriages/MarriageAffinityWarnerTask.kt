package net.perfectdreams.loritta.morenitta.marriages

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.social.MarriageCommand
import net.perfectdreams.loritta.morenitta.scheduledtasks.NamedRunnableCoroutine
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.awt.Container
import java.time.Instant

class MarriageAffinityWarnerTask(val m: LorittaBot, val t: Long) : NamedRunnableCoroutine {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override val taskName = "marriage-affinity-warner-$t-task"

    override suspend fun run() {
        val expiredMarriages = m.pudding.transaction(repetitions = Int.MAX_VALUE) {
            // Check which marriages will be affected by today's marriage decay
            val marriages = UserMarriages.selectAll()
                .where {
                    UserMarriages.affinity lessEq 6 and (UserMarriages.active eq true)
                }
                .toList()

            val marriagesWithAffinity = marriages.map { 
                it[UserMarriages.id] to it[UserMarriages.affinity]
            }.toMap()

            // Get each participant of each marriage that was affected so we can notify them!
            val affectedParticipants = MarriageParticipants.selectAll()
                .where {
                    MarriageParticipants.marriage inList marriages.map { it[UserMarriages.id] }
                }
                .toList()

            // Update the task timer
            updateStoredTimer(m)

            return@transaction MarriagesThatAreGoingToExpireResult(
                marriagesWithAffinity.map { (marriageId, affinity) ->
                    MarriagesThatAreGoingToExpireResult.ExpiredMarriage(
                        marriageId.value,
                        affectedParticipants.filter { participantRow ->
                            participantRow[MarriageParticipants.marriage] == marriageId
                        }.map { it[MarriageParticipants.user] },
                        affinity
                    )
                }
            )
        }

        for (marriage in expiredMarriages.marriages) {
            // TODO: Send the DM!
            for (participantId in marriage.participantIds) {
                try {
                    val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(participantId) ?: continue

                    privateChannel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.components += Container {
                                +TextDisplay(
                                    buildString {
                                        appendLine("### ${Emotes.MarriageRing} Taxa de Afinidade do Casamento")
                                        appendLine("Assim como uma chama, você precisa cuidar da afinidade do seu casamento para ela se manter acesa. Naturalmente, a afinidade diminui 2 pontos a cada dia.")
                                        appendLine()
                                        appendLine("Vocês estão com ${marriage.affinity} pontos de afinidade. Se ela chegar em 0, o casamento acabará! Veja como conseguir mais pontos de afinidade com ${m.commandMentions.marriageView}.")
                                        appendLine()
                                        appendLine("Se um de vocês tiver ${SonhosUtils.getSonhosEmojiOfQuantity(MarriageCommand.MARRIAGE_RESTORE_COST.toLong())} **${MarriageCommand.MARRIAGE_RESTORE_COST} sonhos** quando a afinidade chegar em 0 pontos, ele será automaticamente cobrado e o casamento será restaurado, voltando com 20 pontos sem perder a duração do seu casamento. O casamento também poderá ser restaurado após acabar usando ${m.commandMentions.marriageRestore}, pagando o mesmo preço.")
                                        appendLine()
                                        appendLine("E se sentirem que é hora de seguir caminhos diferentes, a opção de ${m.commandMentions.marriageDivorce} está sempre disponível.")
                                    }
                                )
                            }
                        }
                    ).await()
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to warn the user $participantId about their marriage expiration!" }
                }
            }
        }
    }

    private data class MarriagesThatAreGoingToExpireResult(
        val marriages: List<ExpiredMarriage>
    ) {
        data class ExpiredMarriage(
            val marriageId: Long,
            val participantIds: List<Long>,
            val affinity: Int
        )
    }
}
