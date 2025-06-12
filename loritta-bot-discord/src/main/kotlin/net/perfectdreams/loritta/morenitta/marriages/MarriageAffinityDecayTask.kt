package net.perfectdreams.loritta.morenitta.marriages

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.social.MarriageCommand
import net.perfectdreams.loritta.morenitta.scheduledtasks.NamedRunnableCoroutine
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.StoredMarriageRestoreAutomaticTransaction
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

            val negativeAffinityMarriagesIds = negativeAffinityMarriages.map { it[UserMarriages.id].value }
                .toMutableSet()

            // Get each participant of each marriage that was affected
            val affectedParticipants = MarriageParticipants.selectAll()
                .where {
                    MarriageParticipants.marriage inList negativeAffinityMarriagesIds
                }
                .toList()

            val restoredMarriageIds = mutableSetOf<Long>()
            val restoredMarriages = mutableListOf<ExpiredMarriagesResult.RestoredMarriage>()

            // For each participant, do we have enough sonhos?
            for (marriageId in negativeAffinityMarriagesIds) {
                val participants = affectedParticipants.filter { participantRow ->
                    participantRow[MarriageParticipants.marriage].value == marriageId
                }

                val participantProfiles = Profiles.selectAll().where {
                    Profiles.id inList participants.map { it[MarriageParticipants.user] }
                }.toList()

                for (profile in participantProfiles) {
                    if (profile[Profiles.money] >= MarriageCommand.MARRIAGE_RESTORE_COST) {
                        // We have enough sonhos, so we can do something even better...!

                        // This can be improved after Kotlin 2.2 is released, because we will be able to use continue inside inline lambdas
                        var success = false

                        // Charge the user!
                        SonhosUtils.takeSonhosAndLogToTransactionLog(
                            profile[Profiles.id].value,
                            MarriageCommand.MARRIAGE_RESTORE_COST.toLong(),
                            TransactionType.MARRIAGE,
                            StoredMarriageRestoreAutomaticTransaction,
                            // Carry on...
                            {
                                success = false
                            },
                            {
                                // Update the current affinity
                                UserMarriages.update({ UserMarriages.id eq marriageId }) {
                                    it[UserMarriages.affinity] = MarriageCommand.DEFAULT_AFFINITY
                                }

                                success = true

                                restoredMarriageIds.add(marriageId)

                                restoredMarriages.add(
                                    ExpiredMarriagesResult.RestoredMarriage(
                                        marriageId,
                                        profile[Profiles.id].value,
                                        participants.map { it[MarriageParticipants.user] }
                                    )
                                )
                            }
                        )

                        if (success)
                            break
                    }
                }
            }

            negativeAffinityMarriagesIds.removeAll(restoredMarriageIds)

            // Set all negative affinity marriages to be inactive
            UserMarriages.update({ UserMarriages.id inList negativeAffinityMarriagesIds }) {
                it[UserMarriages.active] = false
                it[UserMarriages.expiredAt] = now
            }

            // And update the task timer
            updateStoredTimer(m)

            return@transaction ExpiredMarriagesResult(
                restoredMarriages,
                negativeAffinityMarriagesIds.map {
                    ExpiredMarriagesResult.ExpiredMarriage(
                        it,
                        affectedParticipants.filter { participantRow ->
                            participantRow[MarriageParticipants.marriage].value == it
                        }.map { it[MarriageParticipants.user] }
                    )
                }
            )
        }

        for (marriage in expiredMarriages.marriages) {
            for (participantId in marriage.participantIds) {
                try {
                    val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(participantId) ?: continue

                    privateChannel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.components += Container {
                                +TextDisplay(
                                    buildString {
                                        appendLine("### Taxa de Afinidade do Casamento")
                                        appendLine("O seu casamento acabou por falta de afinidade... Mas ele foi automaticamente restaurado por <@${marriage.restoredBy}> após ter pagado ${MarriageCommand.MARRIAGE_COST} sonhos!")
                                        appendLine("Evite acabar o seu casamento por falta de afinidade para evitar imprevistos assim.")
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
        for (marriage in expiredMarriages.expiredMarriages) {
            for (participantId in marriage.participantIds) {
                try {
                    val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(participantId) ?: continue

                    privateChannel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.components += Container {
                                +TextDisplay(
                                    buildString {
                                        appendLine("### Taxa de Afinidade do Casamento")
                                        appendLine("O seu casamento acabou por falta de afinidade... Você pode restaurar o seu casamento usando ${m.commandMentions.marriageRestore}")
                                        appendLine("Evite acabar o seu casamento por falta de afinidade para evitar imprevistos assim.")
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
        val marriages: List<RestoredMarriage>,
        val expiredMarriages: List<ExpiredMarriage>
    ) {
        data class RestoredMarriage(
            val marriageId: Long,
            val restoredBy: Long,
            val participantIds: List<Long>
        )

        data class ExpiredMarriage(
            val marriageId: Long,
            val participantIds: List<Long>
        )
    }
}