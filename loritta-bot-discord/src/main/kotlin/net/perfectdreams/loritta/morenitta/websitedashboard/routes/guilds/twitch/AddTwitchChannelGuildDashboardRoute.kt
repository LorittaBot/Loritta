package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.html.body
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
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
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseAllModals
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.ceil

class AddTwitchChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch/add") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        data class AddNewGuildTwitchChannelTransactionResult(
            val valueOfTheDonationKeysEnabledOnThisGuild: Double,
            val premiumTracksCount: Long,
            val state: TwitchAccountTrackState
        )

        val userId = call.parameters["userId"]?.toLong()
        val channelLink = call.parameters["channelLink"]
        val enablePremiumTrack = call.parameters["enablePremiumTrack"]?.toBoolean() ?: false

        val twitchUser = if (userId != null) {
            TwitchWebUtils.getCachedUsersInfoById(website.loritta, userId)
                .first()
        } else if (channelLink != null) {
            val loginStripped = channelLink.removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
                .removePrefix("twitch.tv/")

            TwitchWebUtils.getCachedUsersInfoByLogin(website.loritta, loginStripped)
                .first()
        } else error("Missing Twitch Channel Link OR User ID!") // TODO - bliss-dash: Add proper page?

        val transactionResult = website.loritta.transaction {
            val state = TwitchWebUtils.getTwitchAccountTrackState(twitchUser.id)

            val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                .toList()
                .sumOf { it.value }
                .let { ceil(it) }

            val premiumTracksCount = PremiumTrackTwitchAccounts.selectAll().where {
                PremiumTrackTwitchAccounts.guildId eq guild.idLong
            }.count()

            AddNewGuildTwitchChannelTransactionResult(
                valueOfTheDonationKeysEnabledOnThisGuild,
                premiumTracksCount,
                state
            )
        }

        if (transactionResult.state == TwitchAccountTrackState.UNAUTHORIZED && !enablePremiumTrack) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                if (guildPremiumPlan.maxUnauthorizedTwitchChannels > transactionResult.premiumTracksCount) {
                    blissShowModal(
                        createEmbeddedModal(
                            "Conta não autorizada, mas...",
                            true,
                            {
                                p {
                                    text("A conta que você deseja adicionar não está autorizada na Loritta, mas você tem plano premium!")
                                }

                                p {
                                    text("Você pode seguir até ${guildPremiumPlan.maxUnauthorizedTwitchChannels} contas que não foram autorizadas. Ao autorizar uma conta, outras pessoas podem seguir a conta sem precisar de plano premium, até você remover a conta da sua lista de acompanhamentos premium.")
                                }
                            },
                            listOf(
                                {
                                    defaultModalCloseButton(i18nContext)
                                },
                                {
                                    discordButton(ButtonStyle.PRIMARY) {
                                        attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add"
                                        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
                                        attributes["bliss-vals-query"] = buildJsonObject {
                                            put("userId", twitchUser.id)
                                            put("enablePremiumTrack", true)
                                        }.toString()

                                        text("Continuar")
                                    }
                                }
                            )
                        )
                    )
                } else {
                    blissShowModal(
                        createEmbeddedModal(
                            "Conta não autorizada",
                            true,
                            {
                                p {
                                    text("A conta que você deseja adicionar não está autorizada na Loritta!")
                                }

                                p {
                                    text("Devido a limitações da Twitch, cada solicitação de notificação de livestream custa pontos, exceto se o dono da conta autorizar. Se fosse possível adicionar qualquer conta sem autorização, nós iriamos chegar no limite de solicitações rapidinho, assim não seria possível adicionar novas contas no painel...")
                                }

                                p {
                                    text("Peça para o dono da conta autorizar a conta dela na Loritta, ou compre plano premium na Loritta para poder adicionar contas não autorizadas!")
                                }
                            },
                            listOf {
                                defaultModalCloseButton(i18nContext)
                            }
                        )
                    )
                }
            }
            return
        }

        call.response.header(
            "Bliss-Push-Url",
            URLBuilder("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add?userId=${twitchUser.id}").apply {
                this.parameters.append("userId", twitchUser.id.toString())
                this.parameters.append("enablePremiumTrack", enablePremiumTrack.toString())
            }.buildString()
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
                    // TODO: This shouldn't be here i think
                    blissCloseAllModals()

                    if (call.request.headers["Bliss-Trigger-Element-Id"] == "add-profile") {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.SUCCESS,
                                "Conta encontrada!"
                            )
                        )
                    }

                    goBackToPreviousSectionButton(
                        href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch",
                    ) {
                        text("Voltar para a lista de contas da Twitch")
                    }

                    hr {}

                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            trackedTwitchChannelEditorWithProfile(
                                i18nContext,
                                guild,
                                twitchUser,
                                transactionResult.state,
                                null,
                                null
                            )
                        },
                        {
                            trackedNewProfileEditorSaveBar(
                                i18nContext,
                                guild,
                                "twitch",
                                {
                                    put("userId", twitchUser.id)
                                    put("enablePremiumTrack", enablePremiumTrack)
                                },
                                {
                                    put("twitchUserId", twitchUser.id)
                                    put("enablePremiumTrack", enablePremiumTrack)
                                }
                            )
                        }
                    )
                }
            )
        }
    }
}