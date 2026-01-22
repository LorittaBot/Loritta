package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.lorittapartners

import dev.minn.jda.ktx.messages.MessageCreate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.p
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.BanAppeals
import net.perfectdreams.loritta.cinnamon.pudding.tables.LorittaPartners
import net.perfectdreams.loritta.cinnamon.pudding.tables.PartnerApplications
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils.createAppealReceivedMessage
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils.createApplicationReceivedMessage
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyPartnerApplicationRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyPartnerApplicationResponse
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals.PostBanAppealsRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.partnerapplications.PartnerApplicationResult
import net.perfectdreams.loritta.partnerapplications.PartnerPermissionLevel
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.luna.toasts.EmbeddedToast
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.OffsetDateTime

class PostLorittaPartnersFormRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/loritta-partners/form") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    @Serializable
    data class PartnerApplicationRequest(
        val inviteId: String,
        val serverPurpose: String,
        val whyPartner: String
    )

    override suspend fun onAuthenticatedGuildRequest(
        call: ApplicationCall,
        i18nContext: I18nContext,
        session: LorittaUserSession,
        userPremiumPlan: UserPremiumPlans,
        theme: ColorTheme,
        shimejiSettings: LorittaShimejiSettings,
        guild: Guild,
        guildPremiumPlan: ServerPremiumPlans,
        member: Member
    ) {
        try {
            val request = Json.decodeFromString<PartnerApplicationRequest>(call.receiveText())

            // Validate field lengths
            if (request.inviteId.length !in 1..200 || request.serverPurpose.length !in 1..PartnerApplicationsUtils.FIELD_CHARACTER_LIMIT || request.whyPartner.length !in 1..PartnerApplicationsUtils.FIELD_CHARACTER_LIMIT) {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Formulário Inválido"
                        ) {
                            text("Uma ou mais opções estão inválidas!")
                        }
                    )
                }
                return
            }

            if (PartnerApplicationsUtils.MINIMUM_GUILD_MEMBERS_COUNT > guild.memberCount) {
                call.respondHtmlFragment(HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Formulário Inválido"
                        ) {
                            text("Servidor não cumpre os requisitos de parceria!")
                        }
                    )
                }
                return
            }

            // Validate invite
            val inviteCode = if (request.inviteId.contains("/")) {
                DiscordInviteUtils.getInviteCodeFromUrl(request.inviteId)
            } else {
                request.inviteId
            }

            if (inviteCode == null || !DiscordInviteUtils.inviteCodeRegex.matches(inviteCode)) {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            i18nContext.get(DashboardI18nKeysData.LorittaPartners.Errors.InvalidInvite)
                        )
                    )
                }
                return
            }

            val invite = try {
                Invite.resolve(guild.jda, inviteCode, false).await()
            } catch (e: Exception) {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            i18nContext.get(DashboardI18nKeysData.LorittaPartners.Errors.InvalidInvite)
                        )
                    )
                }
                return
            }

            // Verify invite is for this guild
            val inviteGuild = invite.guild
            if (inviteGuild == null || inviteGuild.idLong != guild.idLong) {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Formulário Inválido"
                        ) {
                            text("O convite do formulário não é deste servidor!")
                        }
                    )
                }
                return
            }

            val submitterPermissionLevel = when {
                member.isOwner -> PartnerPermissionLevel.OWNER
                member.hasPermission(Permission.ADMINISTRATOR) -> PartnerPermissionLevel.ADMINISTRATOR
                member.hasPermission(Permission.MANAGE_SERVER) -> PartnerPermissionLevel.MANAGER
                else -> error("User ${member.idLong} doesn't have any expected permission level! Bug?")
            }

            val result = website.loritta.transaction {
                val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
                val cooldownTime = now.minusSeconds(PartnerApplicationsUtils.APPLICATION_COOLDOWN.inWholeSeconds)

                // Are we already besties??
                val isLorittaPartner = LorittaPartners.selectAll()
                    .where {
                        LorittaPartners.guildId eq guild.idLong
                    }
                    .count() != 0L

                if (isLorittaPartner)
                    return@transaction ApplicationCreationResult.AlreadyPartner

                // Check cooldown by guild ID
                val recentApplication = PartnerApplications.selectAll()
                    .where {
                        PartnerApplications.guildId eq guild.idLong and (PartnerApplications.submittedAt greaterEq cooldownTime)
                    }
                    .firstOrNull()

                if (recentApplication != null)
                    return@transaction ApplicationCreationResult.OnCooldown(now, recentApplication[PartnerApplications.submittedAt].plusSeconds(PartnerApplicationsUtils.APPLICATION_COOLDOWN.inWholeSeconds))

                // Insert into database
                val application = PartnerApplications.insert {
                    it[PartnerApplications.submittedBy] = session.userId
                    it[PartnerApplications.guildId] = guild.idLong
                    it[PartnerApplications.languageId] = website.loritta.languageManager.getIdByI18nContext(i18nContext)
                    it[PartnerApplications.inviteLink] = inviteCode
                    it[PartnerApplications.serverPurpose] = request.serverPurpose
                    it[PartnerApplications.whyPartner] = request.whyPartner
                    it[PartnerApplications.submittedAt] = now
                    it[PartnerApplications.applicationResult] = PartnerApplicationResult.PENDING
                    it[PartnerApplications.reviewedBy] = null
                    it[PartnerApplications.reviewedAt] = null
                    it[PartnerApplications.reviewerNotes] = null
                    it[PartnerApplications.submitterPermissionLevel] = submitterPermissionLevel
                }

                return@transaction ApplicationCreationResult.Success(application)
            }

            when (result) {
                is ApplicationCreationResult.Success -> {
                    val applicationId = result.application[PartnerApplications.id].value

                    val response = LorittaRPC.NotifyPartnerApplication.execute(
                        website.loritta,
                        DiscordUtils.getLorittaClusterForGuildId(website.loritta, website.loritta.config.loritta.partnerApplications.guildId),
                        NotifyPartnerApplicationRequest(
                            applicationId,
                            website.loritta.config.loritta.partnerApplications.guildId,
                            website.loritta.config.loritta.partnerApplications.channelId,
                        )
                    )

                    when (response) {
                        NotifyPartnerApplicationResponse.UserNotFound -> {
                            call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                                blissShowToast(
                                    createEmbeddedToast(
                                        EmbeddedToast.Type.WARN,
                                        "Algo deu errado ao enviar o seu formulário!"
                                    ) {
                                        text("Usuário não encontrado... você já usou a Loritta antes?")
                                    }
                                )
                            }
                            return
                        }
                        NotifyPartnerApplicationResponse.ChannelNotFound -> {
                            call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                                blissShowToast(
                                    createEmbeddedToast(
                                        EmbeddedToast.Type.WARN,
                                        "Algo deu errado ao enviar o seu formulário!"
                                    ) {
                                        text("Canal de Candidaturas do Loritta Partners não foi encontrado")
                                    }
                                )
                            }
                            return
                        }
                        NotifyPartnerApplicationResponse.GuildNotFound -> {
                            call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                                blissShowToast(
                                    createEmbeddedToast(
                                        EmbeddedToast.Type.WARN,
                                        "Algo deu errado ao enviar o seu formulário!"
                                    ) {
                                        text("Servidor de Loritta Partners não encontrado")
                                    }
                                )
                            }
                            return
                        }
                        NotifyPartnerApplicationResponse.Success -> {
                            // We should ALWAYS send the DMs to the user that created the report, even tho they may be sending on behalf of someone else
                            // This is to avoid spamming innocent users
                            val privateChannel = website.loritta.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(session.userId)
                            var successfullySentDM = false

                            if (privateChannel != null) {
                                try {
                                    privateChannel.sendMessage(
                                        MessageCreate {
                                            createApplicationReceivedMessage(website.loritta, result.application[PartnerApplications.id].value)
                                        }
                                    ).await()

                                    successfullySentDM = true
                                } catch (e: Exception) {
                                    logger.warn(e) { "Something went wrong while trying to tell the user that the appeal has been successfully received!" }
                                }
                            }

                            call.respondHtmlFragment(status = HttpStatusCode.OK) {
                                blissSoundEffect("configSaved")
                                blissShowToast(
                                    createEmbeddedToast(
                                        EmbeddedToast.Type.SUCCESS,
                                        i18nContext.get(DashboardI18nKeysData.LorittaPartners.Success.Toast.Title)
                                    )
                                )

                                goBackToPreviousSectionButton("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/loritta-partners") {
                                    text("Voltar")
                                }

                                hr {}

                                heroWrapper {
                                    simpleHeroImage("https://assets.perfectdreams.media/loritta/loritta-support.png")

                                    heroText {
                                        h1 {
                                            text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.Success.Title(result.application[PartnerApplications.id].value.toString())))
                                        }

                                        p {
                                            text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.Success.Description))
                                        }

                                        if (successfullySentDM) {
                                            p {
                                                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.Success.SuccessfullySentDM))
                                            }
                                        } else {
                                            p {
                                                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.Success.FailedSentDM))
                                            }
                                        }

                                        p {
                                            b {
                                                text("Boa sorte!")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is ApplicationCreationResult.OnCooldown -> {
                    call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Você está em cooldown!"
                            ) {
                                text("Você deve esperar ${DateUtils.formatDateDiff(i18nContext, result.now.toInstant(), result.expiresAt.toInstant())} antes de poder enviar outra candidatura!")
                            }
                        )
                    }
                }

                ApplicationCreationResult.AlreadyPartner -> {
                    call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Formulário Inválido"
                            ) {
                                text("O servidor já é parceiro da Loritta!")
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to process partner application for guild ${guild.idLong}" }
            call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Erro ao processar sua candidatura!"
                    )
                )
            }
        }
    }

    private sealed class ApplicationCreationResult {
        data class Success(val application: InsertStatement<Number>) : ApplicationCreationResult()
        data class OnCooldown(
            val now: OffsetDateTime,
            val expiresAt: OffsetDateTime
        ) : ApplicationCreationResult()
        data object AlreadyPartner : ApplicationCreationResult()
    }
}
