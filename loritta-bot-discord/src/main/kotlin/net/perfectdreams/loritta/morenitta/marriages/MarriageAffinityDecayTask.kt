package net.perfectdreams.loritta.morenitta.marriages

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.scheduledtasks.NamedRunnableCoroutine
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class MarriageAffinityDecayTask(val m: LorittaBot) : NamedRunnableCoroutine {
    companion object {
        private const val DAILY_DECAY = 2
        private val logger = KotlinLogging.logger {}
    }

    override val taskName = "marriage-affinity-decay-task"

    override suspend fun run() {
        val expiredMarriages = m.pudding.transaction(repetitions = Int.MAX_VALUE) {
            val now = Instant.now()

            // Apply decay
            UserMarriages.update({ UserMarriages.affinity greater 0 and (UserMarriages.active eq true) }) {
                it[UserMarriages.affinity] = UserMarriages.affinity - DAILY_DECAY
            }

            // Check which marriages are negative (or equal to zero)...
            val negativeAffinityMarriages = UserMarriages.selectAll().where {
                UserMarriages.affinity lessEq 0 and (UserMarriages.active eq true)
            }.toList()

            val negativeAffinityMarriagesIds = negativeAffinityMarriages.map { it[UserMarriages.id] }

            // Set all negative affinity marriages to be inactive
            UserMarriages.update({ UserMarriages.id inList negativeAffinityMarriagesIds }) {
                it[UserMarriages.active] = false
                it[UserMarriages.expiredAt] = now
            }

            // Get each participant of each marriage that was affected so we can notify them!
            val affectedParticipants = MarriageParticipants.selectAll()
                .where {
                    MarriageParticipants.marriage inList negativeAffinityMarriagesIds
                }
                .toList()

            // And update the task timer
            updateStoredTimer(m)

            return@transaction ExpiredMarriagesResult(
                negativeAffinityMarriagesIds.map {
                    ExpiredMarriagesResult.ExpiredMarriage(
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
                                        appendLine("O seu casamento acabou por falta de afinidade... Você pode restaurar o seu casamento usando ${m.commandMentions.marriageRestore}")
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

    private data class ExpiredMarriagesResult(
        val marriages: List<ExpiredMarriage>
    ) {
        data class ExpiredMarriage(
            val marriageId: Long,
            val participantIds: List<Long>
        )
    }
}