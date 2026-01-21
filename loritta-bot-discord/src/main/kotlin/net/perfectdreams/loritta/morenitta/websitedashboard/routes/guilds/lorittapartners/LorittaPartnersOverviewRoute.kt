package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.lorittapartners

import io.ktor.server.application.*
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.style
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.LorittaPartners
import net.perfectdreams.loritta.cinnamon.pudding.tables.PartnerApplications
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.partnerFeatureCard
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.partnerapplications.PartnerApplicationResult
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class LorittaPartnersOverviewRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/loritta-partners") {
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
        // Check if guild has any existing application
        val (isLorittaPartner, existingApplication) = website.loritta.transaction {
            // Verify the guild is an approved partner
            val isLorittaPartner = website.loritta.transaction {
                LorittaPartners.selectAll()
                    .where { LorittaPartners.guildId eq guild.idLong }
                    .count() != 0L
            }

            if (isLorittaPartner)
                return@transaction Pair(true, null)

            val recentApplication = PartnerApplications.selectAll()
                .where { PartnerApplications.guildId eq guild.idLong }
                .orderBy(PartnerApplications.submittedAt, SortOrder.DESC)
                .limit(1)
                .firstOrNull()

            return@transaction Pair(false, recentApplication)
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.LorittaPartners.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.LORITTA_PARTNERS)
                },
                {
                    div {
                        this.id = "partner-application-content"

                        div {
                            div {
                                style = "text-align: center;"

                                h1 {
                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.Title))
                                }

                                for (line in i18nContext.get(DashboardI18nKeysData.LorittaPartners.Description)) {
                                    p {
                                        text(line)
                                    }
                                }
                            }

                            div(classes = "alert alert-danger") {
                                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.BetaStatus.Description))
                            }

                            div(classes = "partner-features") {
                                partnerFeatureCard(
                                    SVGIcons.LockSimple,
                                    i18nContext.get(DashboardI18nKeysData.LorittaPartners.FeatureCards.PrivateServer.Title),
                                    i18nContext.get(DashboardI18nKeysData.LorittaPartners.FeatureCards.PrivateServer.Description)
                                )

                                partnerFeatureCard(
                                    SVGIcons.Star,
                                    i18nContext.get(DashboardI18nKeysData.LorittaPartners.FeatureCards.Feedbacks.Title),
                                    i18nContext.get(DashboardI18nKeysData.LorittaPartners.FeatureCards.Feedbacks.Description)
                                )

                                partnerFeatureCard(
                                    SVGIcons.Fire,
                                    i18nContext.get(DashboardI18nKeysData.LorittaPartners.FeatureCards.Rewards.Title),
                                    i18nContext.get(DashboardI18nKeysData.LorittaPartners.FeatureCards.Rewards.Description)
                                )
                            }
                        }

                        hr {}

                        fun startApplicationButton() {
                            discordButton(ButtonStyle.PRIMARY) {
                                style = "margin-top: 24px;"
                                attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/loritta-partners/form"
                                attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML)"
                                attributes["bliss-push-url:200"] = "true"

                                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.StartApplication))
                            }
                        }

                        if (isLorittaPartner) {
                            // Is partner, yay!!
                            div {
                                style = "text-align: center;"

                                h2 {
                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Approved.Title))
                                }

                                div {
                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Approved.Description))
                                }

                                discordButton(ButtonStyle.SUCCESS) {
                                    style = "margin-top: 24px;"
                                    attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/loritta-partners/join"

                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Approved.JoinServer))
                                }
                            }
                        } else {
                            // Not partner... Let's validate the things then!
                            if (PartnerApplicationsUtils.MINIMUM_GUILD_MEMBERS_COUNT  > guild.memberCount) {
                                div {
                                    style = "text-align: center;"

                                    h2 {
                                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.RequirementsNotMet.Title))
                                    }

                                    div {
                                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.RequirementsNotMet.Description(PartnerApplicationsUtils.MINIMUM_GUILD_MEMBERS_COUNT)))
                                    }
                                }
                            } else if (existingApplication == null) {
                                // No application - show apply button
                                div {
                                    style = "text-align: center;"

                                    h2 {
                                        text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.NotAppliedYet.Title))
                                    }

                                    startApplicationButton()
                                }
                            } else {
                                val applicationResult = existingApplication[PartnerApplications.applicationResult]
                                val submittedAt = existingApplication[PartnerApplications.submittedAt]
                                val applicationId = existingApplication[PartnerApplications.id].value

                                when (applicationResult) {
                                    PartnerApplicationResult.APPROVED, PartnerApplicationResult.PENDING -> {
                                        // The approved clause should NEVER happen, because if someone is a Loritta Partner, they should be in the partner table
                                        // HOWEVER, that could happen if the server becomes a partner and then is revoked from their partnership status
                                        div {
                                            style = "text-align: center;"

                                            h2 {
                                                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Pending.Title))
                                            }

                                            div {
                                                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Pending.Description))
                                            }

                                            div {
                                                style = "margin-top: 12px;"
                                                text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Pending.ApplicationId(applicationId)))
                                            }
                                        }
                                    }

                                    PartnerApplicationResult.DENIED -> {
                                        val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
                                        val cooldownExpiry = submittedAt.plusSeconds(PartnerApplicationsUtils.APPLICATION_COOLDOWN.inWholeSeconds)

                                        if (now.isBefore(cooldownExpiry)) {
                                            // Still on cooldown
                                            div {
                                                style = "text-align: center;"

                                                h2 {
                                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Denied.Title))
                                                }

                                                div {
                                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Denied.Description))
                                                }

                                                div {
                                                    style = "margin-top: 12px;"
                                                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                                    val formattedDate = cooldownExpiry.format(formatter)
                                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Denied.CanReapplyAt(formattedDate)))
                                                }

                                                div {
                                                    style = "margin-top: 12px;"
                                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.Pending.ApplicationId(applicationId)))
                                                }
                                            }
                                        } else {
                                            // Cooldown expired! We can now reapply
                                            div {
                                                style = "text-align: center;"

                                                h2 {
                                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.PreviouslyDenied.Title))
                                                }

                                                div {
                                                    text(i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationStatus.PreviouslyDenied.Description))
                                                }

                                                startApplicationButton()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
