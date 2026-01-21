package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.lorittapartners

import io.ktor.http.*
import io.ktor.server.application.*
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
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.partnerApplicationForm
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.luna.toasts.EmbeddedToast
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class LorittaPartnersFormRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/loritta-partners/form") {
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
        // Check cooldown
        val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
        val cooldownTime = now.minusSeconds(PartnerApplicationsUtils.APPLICATION_COOLDOWN.inWholeSeconds)

        val (isLorittaPartner, recentApplication) = website.loritta.transaction {
            val isLorittaPartner = website.loritta.transaction {
                LorittaPartners.selectAll()
                    .where { LorittaPartners.guildId eq guild.idLong }
                    .count() != 0L
            }

            if (isLorittaPartner)
                return@transaction Pair(isLorittaPartner, null)

            val application = PartnerApplications.selectAll()
                .where { PartnerApplications.guildId eq guild.idLong }
                .orderBy(PartnerApplications.submittedAt, SortOrder.DESC)
                .limit(1)
                .firstOrNull()

            return@transaction Pair(false, application)
        }

        if (isLorittaPartner) {
            call.respondHtml {
                dashboardBase(
                    i18nContext,
                    i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Title),
                    session,
                    theme,
                    shimejiSettings,
                    userPremiumPlan,
                    website.shouldDisplayAds(call, userPremiumPlan, null),
                    {
                        guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.LORITTA_PARTNERS)
                    },
                    {
                        goBackToPreviousSectionButton("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/loritta-partners") {
                            text("Voltar")
                        }

                        hr {}

                        h1 {
                            text("Você já é um Loritta Partner!")
                        }

                        p {
                            text("Você não precisa preencher o formulário, pois você já é um parceiro da Loritta!")
                        }
                    }
                )
            }
            return
        }

        if (recentApplication != null) {
            val submittedAt = recentApplication[PartnerApplications.submittedAt]

            if (submittedAt.isAfter(cooldownTime)) {
                // Still on cooldown
                val expiresAt = submittedAt.plusSeconds(PartnerApplicationsUtils.APPLICATION_COOLDOWN.inWholeSeconds)
                val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

                call.respondHtml {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.LORITTA_PARTNERS)
                        },
                        {
                            goBackToPreviousSectionButton("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/loritta-partners") {
                                text("Voltar")
                            }

                            hr {}

                            h1 {
                                text("Você está em cooldown!")
                            }

                            p {
                                text("Você deve esperar ${DateUtils.formatDateDiff(i18nContext, now.toInstant(), expiresAt.toInstant())} antes de poder enviar outra candidatura!")
                            }
                        }
                    )
                }
                return
            }
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.LorittaPartners.ApplicationForm.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.LORITTA_PARTNERS)
                },
                {
                    goBackToPreviousSectionButton("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/loritta-partners") {
                        text("Voltar")
                    }

                    hr {}

                    partnerApplicationForm(i18nContext, guild, member)
                }
            )
        }
    }
}
