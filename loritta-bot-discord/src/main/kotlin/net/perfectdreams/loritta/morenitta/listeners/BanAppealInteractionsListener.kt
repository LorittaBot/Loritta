package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.banappeals.BanAppealResult
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.BanAppeals
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.banappeals.BanAppeal
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils.createStaffAppealMessage
import net.perfectdreams.loritta.morenitta.interactions.InteractivityManager
import net.perfectdreams.loritta.morenitta.interactions.UnleashedComponentId
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.UserBannedState
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.collections.set

class BanAppealInteractionsListener(val m: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val guild = event.guild ?: return

        if (event.componentId.startsWith("appeal_accept:")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                val deferredReply = event.interaction.deferEdit().submit()

                val serverConfig = m.getOrCreateServerConfig(guild.idLong, true)
                val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val result = m.transaction {
                    val appealType = BanAppeals
                        .innerJoin(BannedUsers)
                        .selectAll()
                        .where {
                            BanAppeals.id eq dbId
                        }
                        .firstOrNull()

                    if (appealType == null)
                        return@transaction AppealAcceptResult.NotFound

                    if (appealType[BanAppeals.appealResult] != BanAppealResult.PENDING)
                        return@transaction AppealAcceptResult.AlreadyReviewed(appealType[BanAppeals.appealResult])

                    val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

                    BanAppeals.update({ BanAppeals.id eq dbId }) {
                        it[BanAppeals.reviewedAt] = now
                        it[BanAppeals.reviewedBy] = event.user.idLong
                        it[BanAppeals.appealResult] = BanAppealResult.APPROVED
                    }

                    return@transaction AppealAcceptResult.Success(
                        appealType[BanAppeals.submittedBy],
                        BanAppeal(
                            appealType[BanAppeals.id].value,
                            appealType[BanAppeals.submittedBy],
                            appealType[BanAppeals.userId],
                            appealType[BanAppeals.whatDidYouDo],
                            appealType[BanAppeals.whyDidYouBreakThem],
                            appealType[BanAppeals.accountIds],
                            appealType[BanAppeals.whyShouldYouBeUnbanned],
                            appealType[BanAppeals.additionalComments],
                            appealType[BanAppeals.files],
                            UserBannedState(
                                appealType[BannedUsers.id].value,
                                appealType[BannedUsers.valid],
                                Instant.fromEpochMilliseconds(appealType[BannedUsers.bannedAt]),
                                appealType[BannedUsers.expiresAt]?.let { Instant.fromEpochMilliseconds(it) },
                                appealType[BannedUsers.reason],
                                appealType[BannedUsers.bannedBy]?.let { UserId(it.toULong()) },
                                appealType[BannedUsers.staffNotes]
                            ),
                            appealType[BanAppeals.submittedAt],
                            event.user.idLong,
                            now,
                            null,
                            BanAppealResult.APPROVED
                        )
                    )
                }

                when (result) {
                    is AppealAcceptResult.Success -> {
                        val submittedBy = m.lorittaShards.retrieveUserInfoById(result.appeal.submittedBy)
                        val appeal = m.lorittaShards.retrieveUserInfoById(result.appeal.userId)

                        deferredReply.await()
                            .editOriginal(
                                MessageEdit {
                                    createStaffAppealMessage(result.appeal, submittedBy, appeal)
                                }
                            )
                            .await()

                        deferredReply.await()
                            .sendMessage(
                                MessageCreate {
                                    styled(
                                        "Apelo aceito!"
                                    )
                                }
                            )
                            .setEphemeral(true)
                            .await()

                        val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(result.userId)
                        if (privateChannel != null) {
                            try {
                                privateChannel.sendMessage(
                                    MessageCreate {
                                        content = "Seu apelo foi aceito!"
                                    }
                                ).await()
                            } catch (e: Exception) {
                                logger.warn(e) { "Something went wrong while trying to tell the user that the appeal has been successfully received!" }
                            }
                        }
                    }
                    is AppealAcceptResult.AlreadyReviewed -> {
                        deferredReply.await()
                            .sendMessage(
                                MessageCreate {
                                    styled(
                                        "Apelo já foi revisado!"
                                    )
                                }
                            )
                            .setEphemeral(true)
                            .await()
                    }
                    AppealAcceptResult.NotFound -> {
                        deferredReply.await()
                            .sendMessage(
                                MessageCreate {
                                    styled(
                                        "Apelo não existe!"
                                    )
                                }
                            )
                            .setEphemeral(true)
                            .await()
                    }
                }
            }
        }

        if (event.componentId.startsWith("appeal_reject:")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                val serverConfig = m.getOrCreateServerConfig(guild.idLong, true)
                val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
                val textInput = modalString("Motivo da Rejeição", TextInputStyle.PARAGRAPH)

                val unleashedComponentId = UnleashedComponentId(UUID.randomUUID())
                m.interactivityManager.modalCallbacks[unleashedComponentId.uniqueId] = InteractivityManager.ModalInteractionCallback(
                    false,
                    { context, args ->
                        val deferredReply = context.event.deferEdit().submit()

                        val serverConfig = m.getOrCreateServerConfig(guild.idLong, true)
                        val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                        val result = m.transaction {
                            val appealType = BanAppeals
                                .innerJoin(BannedUsers)
                                .selectAll()
                                .where {
                                    BanAppeals.id eq dbId
                                }
                                .firstOrNull()

                            if (appealType == null)
                                return@transaction AppealRejectResult.NotFound

                            if (appealType[BanAppeals.appealResult] != BanAppealResult.PENDING)
                                return@transaction AppealRejectResult.AlreadyReviewed(appealType[BanAppeals.appealResult])

                            val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

                            BanAppeals.update({ BanAppeals.id eq dbId }) {
                                it[BanAppeals.reviewedAt] = now
                                it[BanAppeals.reviewedBy] = event.user.idLong
                                it[BanAppeals.appealResult] = BanAppealResult.DENIED
                                it[BanAppeals.reviewerNotes] = args[textInput]
                            }

                            return@transaction AppealRejectResult.Success(
                                appealType[BanAppeals.submittedBy],
                                BanAppeal(
                                    appealType[BanAppeals.id].value,
                                    appealType[BanAppeals.submittedBy],
                                    appealType[BanAppeals.userId],
                                    appealType[BanAppeals.whatDidYouDo],
                                    appealType[BanAppeals.whyDidYouBreakThem],
                                    appealType[BanAppeals.accountIds],
                                    appealType[BanAppeals.whyShouldYouBeUnbanned],
                                    appealType[BanAppeals.additionalComments],
                                    appealType[BanAppeals.files],
                                    UserBannedState(
                                        appealType[BannedUsers.id].value,
                                        appealType[BannedUsers.valid],
                                        Instant.fromEpochMilliseconds(appealType[BannedUsers.bannedAt]),
                                        appealType[BannedUsers.expiresAt]?.let { Instant.fromEpochMilliseconds(it) },
                                        appealType[BannedUsers.reason],
                                        appealType[BannedUsers.bannedBy]?.let { UserId(it.toULong()) },
                                        appealType[BannedUsers.staffNotes]
                                    ),
                                    appealType[BanAppeals.submittedAt],
                                    event.user.idLong,
                                    now,
                                    args[textInput],
                                    BanAppealResult.DENIED
                                )
                            )
                        }


                        when (result) {
                            is AppealRejectResult.Success -> {
                                val submittedBy = m.lorittaShards.retrieveUserInfoById(result.appeal.submittedBy)
                                val appeal = m.lorittaShards.retrieveUserInfoById(result.appeal.userId)

                                deferredReply.await()
                                    .editOriginal(
                                        MessageEdit {
                                            createStaffAppealMessage(result.appeal, submittedBy, appeal)
                                        }
                                    )
                                    .setReplace(true)
                                    .await()

                                deferredReply.await()
                                    .sendMessage(
                                        MessageCreate {
                                            styled(
                                                "Apelo rejeitado!"
                                            )
                                        }
                                    )
                                    .setEphemeral(true)
                                    .await()

                                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(result.userId)
                                if (privateChannel != null) {
                                    try {
                                        privateChannel.sendMessage(
                                            MessageCreate {
                                                content = "Seu apelo foi rejeitado... Motivo: ${args[textInput] ?: "Sem motivo especificado"}"
                                            }
                                        ).await()
                                    } catch (e: Exception) {
                                        logger.warn(e) { "Something went wrong while trying to tell the user that the appeal has been successfully received!" }
                                    }
                                }
                            }
                            is AppealRejectResult.AlreadyReviewed -> {
                                deferredReply.await()
                                    .sendMessage(
                                        MessageCreate {
                                            styled(
                                                "Apelo já foi revisado!"
                                            )
                                        }
                                    )
                                    .setEphemeral(true)
                                    .await()
                            }
                            AppealRejectResult.NotFound -> {
                                deferredReply.await()
                                    .sendMessage(
                                        MessageCreate {
                                            styled(
                                                "Apelo não existe!"
                                            )
                                        }
                                    )
                                    .setEphemeral(true)
                                    .await()
                            }
                        }
                    }
                )

                event.replyModal(
                    unleashedComponentId.toString(),
                    "Rejeição",
                    listOf(textInput.toJDA())
                ).await()
            }
        }
    }

    private sealed class AppealAcceptResult {
        data class Success(val userId: Long, val appeal: BanAppeal) : AppealAcceptResult()
        data class AlreadyReviewed(val result: BanAppealResult) : AppealAcceptResult()
        data object NotFound : AppealAcceptResult()
    }

    private sealed class AppealRejectResult {
        data class Success(val userId: Long, val appeal: BanAppeal) : AppealRejectResult()
        data class AlreadyReviewed(val result: BanAppealResult) : AppealRejectResult()
        data object NotFound : AppealRejectResult()
    }
}