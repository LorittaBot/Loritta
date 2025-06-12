package net.perfectdreams.loritta.morenitta.marriages

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.social.MarriageCommand
import net.perfectdreams.loritta.morenitta.scheduledtasks.NamedRunnableCoroutine
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.StoredMarriageRestoreAutomaticTransaction
import org.jetbrains.exposed.sql.SortOrder
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

                val participantProfiles = Profiles.selectAll()
                    .where {
                        Profiles.id inList participants.map { it[MarriageParticipants.user] }
                    }
                    // Try to get the richest to the poorest, because it is more likely that the richest person will be able to pay for the marriage restore task
                    .orderBy(Profiles.money, SortOrder.DESC)
                    .toList()

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

        val expiresAfter = Instant.now().plusMillis(604_800_000)

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
                                        appendLine("### ${Emotes.LoriHeart} Uma Segunda Chance para o Amor!")
                                        appendLine("A afinidade do seu casamento chegou a zero, levando ao fim do seu casamento...")
                                        appendLine()
                                        appendLine("Felizmente <@${marriage.restoredBy}> tinha **${SonhosUtils.getSonhosEmojiOfQuantity(MarriageCommand.MARRIAGE_RESTORE_COST.toLong())} ${MarriageCommand.MARRIAGE_RESTORE_COST} sonhos** no bolso e, com eles, a afinidade do casamento foi restaurada! O seu casamento continuará, agora com ${MarriageCommand.DEFAULT_AFFINITY} pontos de afinidade.")
                                        appendLine()
                                        appendLine("Cuidem bem um do outro para que o casamento não chegue ao fim. Para saber como conseguir pontos de afinidade, use ${m.commandMentions.marriageView}!")
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
                                        appendLine("### ${Emotes.LoriSob} Seu Casamento Acabou...")
                                        appendLine("Infelizmente o seu casamento acabou por falta de afinidade...")
                                        appendLine()
                                        appendLine("Mas nem tudo está perdido! Vocês têm uma chance de reacender essa chama e continuar a história de onde pararam usando ${m.commandMentions.marriageRestore}. Mas cuidado, vocês tem até ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(expiresAfter)} para poder restaurar o casamento!")
                                        appendLine()
                                        appendLine("**Que esta experiência seja um lembrete:** Os laços mais fortes são aqueles que cuidamos todos os dias.")
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