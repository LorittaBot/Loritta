package net.perfectdreams.loritta.morenitta.marriages

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
import mu.KotlinLogging
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.morenitta.LorittaBot
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
        private val logger = KotlinLogging.logger {}
    }

    override val taskName = "marriage-affinity-warner-$t-task"

    override suspend fun run() {
        val expiredMarriages = m.pudding.transaction(repetitions = Int.MAX_VALUE) {
            // Check which marriages will be affected by today's marriage decay
            val marriages = UserMarriages.selectAll()
                .where {
                    UserMarriages.affinity lessEq 2 and (UserMarriages.active eq true)
                }
                .toList()

            val negativeAffinityMarriagesIds = marriages.map { it[UserMarriages.id] }

            // Get each participant of each marriage that was affected so we can notify them!
            val affectedParticipants = MarriageParticipants.selectAll()
                .where {
                    MarriageParticipants.marriage inList marriages.map { it[UserMarriages.id] }
                }
                .toList()

            // Update the task timer
            updateStoredTimer(m)

            return@transaction MarriagesThatAreGoingToExpireResult(
                negativeAffinityMarriagesIds.map {
                    MarriagesThatAreGoingToExpireResult.ExpiredMarriage(
                        it.value,
                        affectedParticipants.filter { participantRow ->
                            participantRow[MarriageParticipants.marriage] == it
                        }.map { it[MarriageParticipants.user] }
                    )
                }
            )
        }

        for (marriage in expiredMarriages.marriages) {
            // TODO: Send the DM!
            for (participantId in marriage.participantIds) {
                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(participantId) ?: continue
                try {
                    privateChannel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.components += Container {
                                +TextDisplay(
                                    buildString {
                                        appendLine("### Taxa de Afinidade do Casamento")
                                        appendLine("O seu casamento irá acabar em breve se você não conseguir mais afinidade! Para conseguir mais afinidade, veja as informações do seu casamento com ${m.commandMentions.marriageView}")
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
            val participantIds: List<Long>
        )
    }
}