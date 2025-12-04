package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.banappeals.BanAppealResult
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BanAppeals
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.banappeals.BanAppeal
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils.createAppealAcceptedMessage
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils.createAppealDeniedMessage
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils.createStaffAppealMessage
import net.perfectdreams.loritta.morenitta.interactions.InteractivityManager
import net.perfectdreams.loritta.morenitta.interactions.UnleashedComponentId
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.serializable.UserBannedState
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.collections.set

class BanAppealInteractionsListener(val m: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    private fun wrapBanAppealForResult(
        appealRow: ResultRow,
        appealResult: BanAppealResult,
        reviewedBy: Long,
        reviewedAt: OffsetDateTime,
        reviewerNotes: String?
    ): BanAppeal {
        return BanAppeal(
            appealRow[BanAppeals.id].value,
            appealRow[BanAppeals.submittedBy],
            appealRow[BanAppeals.userId],
            appealRow[BanAppeals.whatDidYouDo],
            appealRow[BanAppeals.whyDidYouBreakThem],
            appealRow[BanAppeals.accountIds],
            appealRow[BanAppeals.whyShouldYouBeUnbanned],
            appealRow[BanAppeals.additionalComments],
            appealRow[BanAppeals.files],
            UserBannedState(
                appealRow[BannedUsers.id].value,
                appealRow[BannedUsers.valid],
                Instant.fromEpochMilliseconds(appealRow[BannedUsers.bannedAt]),
                appealRow[BannedUsers.expiresAt]?.let { Instant.fromEpochMilliseconds(it) },
                appealRow[BannedUsers.reason],
                appealRow[BannedUsers.bannedBy]?.let { UserId(it.toULong()) },
                appealRow[BannedUsers.staffNotes]
            ),
            appealRow[BanAppeals.submittedAt],
            appealRow[BanAppeals.languageId],
            reviewedBy,
            reviewedAt,
            reviewerNotes,
            appealResult
        )
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val guild = event.guild ?: return

        if (event.componentId.startsWith("appeal_accept:")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                onAppealAccept(event, guild, dbId)
            }
        }

        if (event.componentId.startsWith("appeal_reject:")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                onAppealReject(event, guild, dbId)
            }
        }
    }

    suspend fun onAppealAccept(event: ButtonInteractionEvent, guild: Guild, dbId: Long) {
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
                wrapBanAppealForResult(
                    appealType,
                    BanAppealResult.APPROVED,
                    event.user.idLong,
                    now,
                    null
                )
            )
        }

        when (result) {
            is AppealAcceptResult.Success -> {
                val (submittedBy, appeal) = BanAppealsUtils.getCachedUserInfoForAppeal(m, result.appeal)

                if (submittedBy == null) {
                    deferredReply.await()
                        .sendMessage(
                            MessageCreate {
                                styled(
                                    "Apelo aceito, mas parece que os dados de quem enviou o apelo não existem! Bug?"
                                )
                            }
                        )
                        .setEphemeral(true)
                        .await()
                    return
                }

                if (appeal == null) {
                    deferredReply.await()
                        .sendMessage(
                            MessageCreate {
                                styled(
                                    "Apelo aceito, mas parece que os dados da pessoa que enviou o apelo não existem! Bug?"
                                )
                            }
                        )
                        .setEphemeral(true)
                        .await()
                    return
                }

                deferredReply.await()
                    .editOriginal(
                        MessageEdit {
                            createStaffAppealMessage(m, result.appeal, submittedBy, appeal)
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

                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(result.appeal.submittedBy)
                if (privateChannel != null) {
                    try {
                        privateChannel.sendMessage(
                            MessageCreate {
                                createAppealAcceptedMessage(m, result.appeal.id, event.user)
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

    suspend fun onAppealReject(event: ButtonInteractionEvent, guild: Guild, dbId: Long) {
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
                        wrapBanAppealForResult(
                            appealType,
                            BanAppealResult.DENIED,
                            event.user.idLong,
                            now,
                            args[textInput],
                        )
                    )
                }

                when (result) {
                    is AppealRejectResult.Success -> {
                        val (submittedBy, appeal) = BanAppealsUtils.getCachedUserInfoForAppeal(m, result.appeal)

                        if (submittedBy == null) {
                            deferredReply.await()
                                .sendMessage(
                                    MessageCreate {
                                        styled(
                                            "Apelo rejeitado, mas parece que os dados de quem enviou o apelo não existem! Bug?"
                                        )
                                    }
                                )
                                .setEphemeral(true)
                                .await()
                            return@ModalInteractionCallback
                        }

                        if (appeal == null) {
                            deferredReply.await()
                                .sendMessage(
                                    MessageCreate {
                                        styled(
                                            "Apelo rejeitado, mas parece que os dados da pessoa que enviou o apelo não existem! Bug?"
                                        )
                                    }
                                )
                                .setEphemeral(true)
                                .await()
                            return@ModalInteractionCallback
                        }

                        deferredReply.await()
                            .editOriginal(
                                MessageEdit {
                                    createStaffAppealMessage(m, result.appeal, submittedBy, appeal)
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

                        val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(result.appeal.submittedBy)
                        if (privateChannel != null) {
                            try {
                                privateChannel.sendMessage(
                                    MessageCreate {
                                       createAppealDeniedMessage(m, result.appeal.id, args[textInput], result.appeal.submittedAt.plusSeconds(BanAppealsUtils.BAN_APPEAL_COOLDOWN.inWholeSeconds).toInstant())
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

    private sealed class AppealAcceptResult {
        data class Success(val appeal: BanAppeal) : AppealAcceptResult()
        data class AlreadyReviewed(val result: BanAppealResult) : AppealAcceptResult()
        data object NotFound : AppealAcceptResult()
    }

    private sealed class AppealRejectResult {
        data class Success(val appeal: BanAppeal) : AppealRejectResult()
        data class AlreadyReviewed(val result: BanAppealResult) : AppealRejectResult()
        data object NotFound : AppealRejectResult()
    }
}