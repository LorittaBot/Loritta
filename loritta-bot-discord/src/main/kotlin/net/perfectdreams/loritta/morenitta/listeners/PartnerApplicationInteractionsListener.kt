package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.LorittaPartners
import net.perfectdreams.loritta.cinnamon.pudding.tables.PartnerApplications
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.InteractivityManager
import net.perfectdreams.loritta.morenitta.interactions.UnleashedComponentId
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerGuildInfo
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplication
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationData
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils.createApplicationAcceptedMessage
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils.createApplicationDeniedMessage
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils.createStaffApplicationMessage
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.rpc.payloads.QueryGuildInfoRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.QueryGuildInfoResponse
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.partnerapplications.PartnerApplicationResult
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.collections.set

class PartnerApplicationInteractionsListener(val m: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    private fun wrapPartnerApplicationForResult(
        applicationRow: ResultRow,
        applicationResult: PartnerApplicationResult,
        reviewedBy: Long,
        reviewedAt: OffsetDateTime,
        reviewerNotes: String?
    ): PartnerApplication {
        return PartnerApplication(
            applicationRow[PartnerApplications.id].value,
            applicationRow[PartnerApplications.submittedBy],
            applicationRow[PartnerApplications.guildId],
            applicationRow[PartnerApplications.languageId],
            applicationRow[PartnerApplications.inviteLink],
            applicationRow[PartnerApplications.serverPurpose],
            applicationRow[PartnerApplications.whyPartner],
            applicationRow[PartnerApplications.submittedAt],
            reviewedBy,
            reviewedAt,
            reviewerNotes,
            applicationResult,
            applicationRow[PartnerApplications.submitterPermissionLevel]
        )
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val guild = event.guild ?: return

        if (event.componentId.startsWith("partner_accept:")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                onApplicationAccept(event, guild, dbId)
            }
        }

        if (event.componentId.startsWith("partner_reject:")) {
            val dbId = event.componentId.substringAfter(":").toLong()

            GlobalScope.launch {
                onApplicationReject(event, guild, dbId)
            }
        }
    }

    suspend fun onApplicationAccept(event: ButtonInteractionEvent, guild: Guild, dbId: Long) {
        val deferredReply = event.interaction.deferEdit().submit()

        val serverConfig = m.getOrCreateServerConfig(guild.idLong, true)
        val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

        val result = m.transaction {
            val applicationType = PartnerApplications
                .selectAll()
                .where {
                    PartnerApplications.id eq dbId
                }
                .firstOrNull()

            if (applicationType == null)
                return@transaction ApplicationAcceptResult.NotFound

            if (applicationType[PartnerApplications.applicationResult] != PartnerApplicationResult.PENDING)
                return@transaction ApplicationAcceptResult.AlreadyReviewed(applicationType[PartnerApplications.applicationResult])

            val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

            PartnerApplications.update({ PartnerApplications.id eq dbId }) {
                it[PartnerApplications.reviewedAt] = now
                it[PartnerApplications.reviewedBy] = event.user.idLong
                it[PartnerApplications.applicationResult] = PartnerApplicationResult.APPROVED
            }

            val alreadyIsPartner = LorittaPartners.selectAll()
                .where {
                    LorittaPartners.guildId eq applicationType[PartnerApplications.guildId]
                }
                .count() != 0L

            if (!alreadyIsPartner) {
                // This should NORMALLY not happen, but who knows, maybe Loritta may have sent the same application message twice...
                LorittaPartners.insert {
                    it[guildId] = applicationType[PartnerApplications.guildId]
                    it[acceptedBy] = event.user.idLong
                    it[acceptedAt] = now
                }
            }

            return@transaction ApplicationAcceptResult.Success(
                wrapPartnerApplicationForResult(
                    applicationType,
                    PartnerApplicationResult.APPROVED,
                    event.user.idLong,
                    now,
                    null
                )
            )
        }

        when (result) {
            is ApplicationAcceptResult.Success -> {
                val submittedByUser = m.lorittaShards.retrieveUserInfoById(result.application.submittedBy)

                if (submittedByUser == null) {
                    deferredReply.await()
                        .sendMessage(
                            MessageCreate {
                                styled(
                                    "Candidatura aceita, mas parece que os dados de quem enviou a candidatura não existem! Bug?"
                                )
                            }
                        )
                        .setEphemeral(true)
                        .await()
                    return
                }

                // Query guild info from the correct cluster since the guild might not be present in this cluster
                val applicationGuildCluster = DiscordUtils.getLorittaClusterForGuildId(m, result.application.guildId)
                val applicationGuildInfoResponse = LorittaRPC.QueryGuildInfo.execute(
                    m,
                    applicationGuildCluster,
                    QueryGuildInfoRequest(result.application.guildId)
                )

                val applicationGuildInfo = when (applicationGuildInfoResponse) {
                    is QueryGuildInfoResponse.Success -> PartnerGuildInfo(
                        name = applicationGuildInfoResponse.name,
                        memberCount = applicationGuildInfoResponse.memberCount,
                        iconUrl = applicationGuildInfoResponse.iconUrl,
                        ownerId = applicationGuildInfoResponse.ownerId
                    )
                    is QueryGuildInfoResponse.GuildNotFound -> null
                }

                deferredReply.await()
                    .editOriginal(
                        MessageEdit {
                            createStaffApplicationMessage(
                                m,
                                PartnerApplicationData(
                                    result.application.id,
                                    result.application.guildId,
                                    result.application.inviteLink,
                                    result.application.serverPurpose,
                                    result.application.whyPartner,
                                    result.application.applicationResult,
                                    result.application.submitterPermissionLevel,
                                    result.application.reviewedBy,
                                    result.application.reviewedAt,
                                    result.application.reviewerNotes
                                ),
                                submittedByUser,
                                applicationGuildInfo
                            )
                        }
                    )
                    .await()

                deferredReply.await()
                    .sendMessage(
                        MessageCreate {
                            styled(
                                "Candidatura aceita!"
                            )
                        }
                    )
                    .setEphemeral(true)
                    .await()

                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(result.application.submittedBy)
                if (privateChannel != null) {
                    try {
                        privateChannel.sendMessage(
                            MessageCreate {
                                createApplicationAcceptedMessage(m, result.application.id, event.user)
                            }
                        ).await()
                    } catch (e: Exception) {
                        logger.warn(e) { "Something went wrong while trying to tell the user that the partner application has been accepted!" }
                    }
                }
            }
            is ApplicationAcceptResult.AlreadyReviewed -> {
                deferredReply.await()
                    .sendMessage(
                        MessageCreate {
                            styled(
                                "Candidatura já foi revisada!"
                            )
                        }
                    )
                    .setEphemeral(true)
                    .await()
            }
            ApplicationAcceptResult.NotFound -> {
                deferredReply.await()
                    .sendMessage(
                        MessageCreate {
                            styled(
                                "Candidatura não existe!"
                            )
                        }
                    )
                    .setEphemeral(true)
                    .await()
            }
        }
    }

    suspend fun onApplicationReject(event: ButtonInteractionEvent, guild: Guild, dbId: Long) {
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
                    val applicationType = PartnerApplications
                        .selectAll()
                        .where {
                            PartnerApplications.id eq dbId
                        }
                        .firstOrNull()

                    if (applicationType == null)
                        return@transaction ApplicationRejectResult.NotFound

                    if (applicationType[PartnerApplications.applicationResult] != PartnerApplicationResult.PENDING)
                        return@transaction ApplicationRejectResult.AlreadyReviewed(applicationType[PartnerApplications.applicationResult])

                    val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

                    PartnerApplications.update({ PartnerApplications.id eq dbId }) {
                        it[PartnerApplications.reviewedAt] = now
                        it[PartnerApplications.reviewedBy] = event.user.idLong
                        it[PartnerApplications.applicationResult] = PartnerApplicationResult.DENIED
                        it[PartnerApplications.reviewerNotes] = args[textInput]
                    }

                    return@transaction ApplicationRejectResult.Success(
                        wrapPartnerApplicationForResult(
                            applicationType,
                            PartnerApplicationResult.DENIED,
                            event.user.idLong,
                            now,
                            args[textInput],
                        )
                    )
                }

                when (result) {
                    is ApplicationRejectResult.Success -> {
                        val submittedByUser = m.lorittaShards.retrieveUserInfoById(result.application.submittedBy)

                        if (submittedByUser == null) {
                            deferredReply.await()
                                .sendMessage(
                                    MessageCreate {
                                        styled(
                                            "Candidatura rejeitada, mas parece que os dados de quem enviou a candidatura não existem! Bug?"
                                        )
                                    }
                                )
                                .setEphemeral(true)
                                .await()
                            return@ModalInteractionCallback
                        }

                        // Query guild info from the correct cluster since the guild might not be present in this cluster
                        val applicationGuildCluster = DiscordUtils.getLorittaClusterForGuildId(m, result.application.guildId)
                        val applicationGuildInfoResponse = LorittaRPC.QueryGuildInfo.execute(
                            m,
                            applicationGuildCluster,
                            QueryGuildInfoRequest(result.application.guildId)
                        )

                        val applicationGuildInfo = when (applicationGuildInfoResponse) {
                            is QueryGuildInfoResponse.Success -> PartnerGuildInfo(
                                name = applicationGuildInfoResponse.name,
                                memberCount = applicationGuildInfoResponse.memberCount,
                                iconUrl = applicationGuildInfoResponse.iconUrl,
                                ownerId = applicationGuildInfoResponse.ownerId
                            )
                            is QueryGuildInfoResponse.GuildNotFound -> null
                        }

                        deferredReply.await()
                            .editOriginal(
                                MessageEdit {
                                    createStaffApplicationMessage(
                                        m,
                                        PartnerApplicationData(
                                            result.application.id,
                                            result.application.guildId,
                                            result.application.inviteLink,
                                            result.application.serverPurpose,
                                            result.application.whyPartner,
                                            result.application.applicationResult,
                                            result.application.submitterPermissionLevel,
                                            result.application.reviewedBy,
                                            result.application.reviewedAt,
                                            result.application.reviewerNotes
                                        ),
                                        submittedByUser,
                                        applicationGuildInfo
                                    )
                                }
                            )
                            .setReplace(true)
                            .await()

                        deferredReply.await()
                            .sendMessage(
                                MessageCreate {
                                    styled(
                                        "Candidatura rejeitada!"
                                    )
                                }
                            )
                            .setEphemeral(true)
                            .await()

                        val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(result.application.submittedBy)
                        if (privateChannel != null) {
                            try {
                                privateChannel.sendMessage(
                                    MessageCreate {
                                        createApplicationDeniedMessage(m, result.application.id, args[textInput], result.application.submittedAt.plusSeconds(PartnerApplicationsUtils.APPLICATION_COOLDOWN.inWholeSeconds).toInstant())
                                    }
                                ).await()
                            } catch (e: Exception) {
                                logger.warn(e) { "Something went wrong while trying to tell the user that the partner application has been rejected!" }
                            }
                        }
                    }
                    is ApplicationRejectResult.AlreadyReviewed -> {
                        deferredReply.await()
                            .sendMessage(
                                MessageCreate {
                                    styled(
                                        "Candidatura já foi revisada!"
                                    )
                                }
                            )
                            .setEphemeral(true)
                            .await()
                    }
                    ApplicationRejectResult.NotFound -> {
                        deferredReply.await()
                            .sendMessage(
                                MessageCreate {
                                    styled(
                                        "Candidatura não existe!"
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
            "Rejeição de Candidatura",
            listOf(textInput.toJDA())
        ).await()
    }

    private sealed class ApplicationAcceptResult {
        data class Success(val application: PartnerApplication) : ApplicationAcceptResult()
        data class AlreadyReviewed(val result: PartnerApplicationResult) : ApplicationAcceptResult()
        data object NotFound : ApplicationAcceptResult()
    }

    private sealed class ApplicationRejectResult {
        data class Success(val application: PartnerApplication) : ApplicationRejectResult()
        data class AlreadyReviewed(val result: PartnerApplicationResult) : ApplicationRejectResult()
        data object NotFound : ApplicationRejectResult()
    }
}
