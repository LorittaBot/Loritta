package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.TwitchWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.TwitchUser
import net.perfectdreams.loritta.serializable.config.GuildTwitchConfig
import net.perfectdreams.loritta.serializable.config.PremiumTrackTwitchAccount
import net.perfectdreams.loritta.serializable.config.TrackedTwitchAccount
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.ceil

class TwitchGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val (twitchAccounts, premiumTrackTwitchAccounts, valueOfTheDonationKeysEnabledOnThisGuild) = website.loritta.transaction {
            val twitchAccounts = TrackedTwitchAccounts.selectAll().where { TrackedTwitchAccounts.guildId eq guild.idLong }
                .map {
                    val state = TwitchWebUtils.getTwitchAccountTrackState(it[TrackedTwitchAccounts.twitchUserId])

                    Pair(
                        state,
                        TrackedTwitchAccount(
                            it[TrackedTwitchAccounts.id].value,
                            it[TrackedTwitchAccounts.twitchUserId],
                            it[TrackedTwitchAccounts.channelId],
                            it[TrackedTwitchAccounts.message]
                        )
                    )
                }

            val premiumTrackTwitchAccounts = PremiumTrackTwitchAccounts.selectAll().where {
                PremiumTrackTwitchAccounts.guildId eq guild.idLong
            }.map {
                PremiumTrackTwitchAccount(
                    it[PremiumTrackTwitchAccounts.id].value,
                    it[PremiumTrackTwitchAccounts.twitchUserId]
                )
            }

            val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                .toList()
                .sumOf { it.value }
                .let { ceil(it) }

            Triple(twitchAccounts, premiumTrackTwitchAccounts, valueOfTheDonationKeysEnabledOnThisGuild)
        }

        val accountsInfo = TwitchWebUtils.getCachedUsersInfoById(
            website.loritta,
            *((twitchAccounts.map { it.second.twitchUserId } + premiumTrackTwitchAccounts.map { it.twitchUserId }).toSet()).toLongArray()
        )

        val twitchConfig = GuildTwitchConfig(
            twitchAccounts.map { trackedTwitchAccount ->
                GuildTwitchConfig.TrackedTwitchAccountWithTwitchUserAndTrackingState(
                    trackedTwitchAccount.first,
                    trackedTwitchAccount.second,
                    accountsInfo.firstOrNull { it.id == trackedTwitchAccount.second.twitchUserId }?.let {
                        TwitchUser(it.id, it.login, it.displayName, it.profileImageUrl)
                    }
                )
            },
            premiumTrackTwitchAccounts.map { trackedTwitchAccount ->
                GuildTwitchConfig.PremiumTrackTwitchAccountWithTwitchUser(
                    trackedTwitchAccount,
                    accountsInfo.firstOrNull { it.id == trackedTwitchAccount.twitchUserId }?.let {
                        TwitchUser(it.id, it.login, it.displayName, it.profileImageUrl)
                    }
                )
            }
        )

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Twitch.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.TWITCH)
                },
                {
                    heroWrapper {
                        heroText {
                            h1 {
                                text(i18nContext.get(DashboardI18nKeysData.Twitch.Title))
                            }

                            p {
                                text("Anuncie para seus membros quando você entra ao vivo na Twitch! Assim, seus fãs não irão perder as suas lives.")
                            }
                        }
                    }

                    hr {}

                    trackedTwitchChannelsSection(
                        website.loritta,
                        i18nContext,
                        guild,
                        twitchConfig.trackedTwitchAccounts.map {
                            val profileInfo = it.twitchUser

                            TrackedProfile(
                                profileInfo?.login,
                                profileInfo?.profileImageUrl,
                                it.trackedInfo.twitchUserId.toString(),
                                it.trackedInfo.id,
                                it.trackedInfo.channelId,
                            )
                        }
                    )

                    hr {}

                    heroWrapper {
                        heroText {
                            h2 {
                                text("Acompanhamentos Premium")
                            }

                            p {
                                text("Servidores premium podem seguir contas que não foram autorizadas na Loritta. Aqui, você encontrará todas as contas com o recurso de acompanhamento premium ativado!")
                            }
                        }
                    }

                    cardsWithHeader {
                        cardHeader {
                            cardHeaderInfo {
                                cardHeaderTitle {
                                    text("Canais com Acompanhamento Premium")
                                }

                                cardHeaderDescription {
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.Twitch.Channels(twitchConfig.premiumTrackTwitchAccounts.size)))
                                }
                            }
                        }

                        if (twitchConfig.premiumTrackTwitchAccounts.isNotEmpty()) {
                            div(classes = "cards") {
                                for (profile in twitchConfig.premiumTrackTwitchAccounts) {
                                    div(classes = "card") {
                                        style = "flex-direction: row; align-items: center; gap: 0.5em;"

                                        div {
                                            style = "flex-grow: 1; display: flex;\n" +
                                                    "  align-items: center;\n" +
                                                    "  flex-direction: row;\n" +
                                                    "  gap: 16px;"

                                            img(src = profile.twitchUser?.profileImageUrl) {
                                                style = "border-radius: 99999px;"
                                                width = "48"
                                                height = "48"
                                            }

                                            div {
                                                style = "display: flex; flex-direction: column;"

                                                div {
                                                    span {
                                                        style = "font-weight: bold;"

                                                        text(profile.twitchUser?.displayName ?: "???")
                                                    }
                                                }

                                                div {
                                                    text(profile.trackedInfo.twitchUserId.toString())
                                                }
                                            }
                                        }

                                        div {
                                            style = "display: grid;grid-template-columns: 1fr;grid-column-gap: 0.5em;"

                                            discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                                                openModalOnClick(
                                                    createEmbeddedConfirmDeletionModal(i18nContext) {
                                                        attributes["bliss-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/premium-tracks/${profile.trackedInfo.id}"
                                                    }
                                                )

                                                text("Excluir")
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            emptySection(i18nContext)
                        }
                    }
                }
            )
        }
    }
}