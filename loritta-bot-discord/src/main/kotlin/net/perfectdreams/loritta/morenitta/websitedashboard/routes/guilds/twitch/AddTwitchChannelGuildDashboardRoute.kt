package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.html.body
import kotlinx.html.div
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
import net.perfectdreams.loritta.dashboard.EmbeddedToast
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
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.ceil

class AddTwitchChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch/add") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
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
            val plan = ServerPremiumPlans.getPlanFromValue(transactionResult.valueOfTheDonationKeysEnabledOnThisGuild)

            call.respondHtml(
                createHTML()
                    .body {
                        if (plan.maxUnauthorizedTwitchChannels > transactionResult.premiumTracksCount) {
                            blissShowModal(
                                createEmbeddedModal(
                                    "Conta não autorizada, mas...",
                                    true,
                                    {
                                        p {
                                            text("A conta que você deseja adicionar não está autorizada na Loritta, mas você tem plano premium!")
                                        }

                                        p {
                                            text("Você pode seguir até ${plan.maxUnauthorizedTwitchChannels} contas que não foram autorizadas. Ao autorizar uma conta, outras pessoas podem seguir a conta sem precisar de plano premium, até você remover a conta da sua lista de acompanhamentos premium.")
                                        }
                                    },
                                    listOf(
                                        {
                                            defaultModalCloseButton(i18nContext)
                                        },
                                        {
                                            discordButton(ButtonStyle.PRIMARY) {
                                                attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add"
                                                attributes["bliss-swap:200"] = "#right-sidebar-contents -> #right-sidebar-contents (innerHTML)"
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
                    },
                status = HttpStatusCode.BadRequest
            )

            return
        }

        call.response.header(
            "Bliss-Push-Url",
            URLBuilder("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add?userId=${twitchUser.id}").apply {
                this.parameters.append("userId", twitchUser.id.toString())
                this.parameters.append("enablePremiumTrack", enablePremiumTrack.toString())
            }.buildString()
        )

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Twitch.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.TWITCH)
                        },
                        {
                            // TODO: This shouldn't be here i think
                            blissCloseModal()

                            if (call.request.headers["Bliss-Trigger-Element-Id"] == "add-profile") {
                                blissCloseModal()
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
                                {
                                    trackedProfileHeader(twitchUser.displayName, twitchUser.profileImageUrl)

                                    when (transactionResult.state) {
                                        TwitchAccountTrackState.AUTHORIZED -> {
                                            div(classes = "alert alert-success") {
                                                text("O canal foi autorizado pelo dono, então você receberá notificações quando o canal entrar ao vivo!")
                                            }
                                        }
                                        TwitchAccountTrackState.ALWAYS_TRACK_USER -> {
                                            div(classes = "alert alert-success") {
                                                text("O canal não está autorizado, mas ela está na minha lista especial de \"pessoas tão incríveis que não preciso pedir autorização\". Você receberá notificações quando o canal entrar ao vivo.")
                                            }
                                        }
                                        TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
                                            div(classes = "alert alert-success") {
                                                text("O canal não está autorizado, mas você colocou ele na lista de acompanhamentos premium! Você receberá notificações quando o canal entrar ao vivo.")
                                            }
                                        }
                                        TwitchAccountTrackState.UNAUTHORIZED -> {
                                            div(classes = "alert alert-danger") {
                                                text("O canal não está autorizado! Você só receberá notificações quando o canal for autorizado na Loritta.")
                                            }
                                        }
                                    }

                                    sectionConfig {
                                        trackedTwitchChannelEditor(
                                            i18nContext,
                                            guild,
                                            null,
                                            "Ao vivo yayyy!"
                                        )
                                    }
                                },
                                {
                                    saveBar(
                                        i18nContext,
                                        true,
                                        {
                                            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add"
                                            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                                            attributes["bliss-headers"] = buildJsonObject {
                                                put("Loritta-Configuration-Reset", "true")
                                            }.toString()
                                            attributes["bliss-vals-query"] = buildJsonObject {
                                                put("userId", twitchUser.id)
                                                put("enablePremiumTrack", enablePremiumTrack)
                                            }.toString()
                                        }
                                    ) {
                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch"
                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-content-and-save-bar-wrapper (innerHTML)"
                                        attributes["bliss-include-json"] = "#section-config"
                                        attributes["bliss-vals-json"] = buildJsonObject {
                                            put("twitchUserId", twitchUser.id)
                                            put("enablePremiumTrack", enablePremiumTrack)
                                        }.toString()
                                    }
                                }
                            )
                        }
                    )
                }
        )
    }
}