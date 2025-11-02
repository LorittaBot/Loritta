package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.img
import kotlinx.html.style
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.guildPremiumKeysAndPremiumInfoPlan(
    i18nContext: I18nContext,
    guild: Guild,
    session: UserSession,
    plan: ServerPremiumPlans,
    guildPremiumKeys: List<ResultRow>,
    userPremiumKeys: List<ResultRow>
) {
    val now = System.currentTimeMillis()

    h2 {
        text("Plano Atual do Servidor")
    }

    b {
        when (plan) {
            ServerPremiumPlans.Complete -> text("Completo")
            ServerPremiumPlans.Essential -> text("Essencial")
            ServerPremiumPlans.Recommended -> text("Recomendado")
            ServerPremiumPlans.Free -> text("Grátis")
        }
    }

    hr {}

    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.KeysOnThisGuild.Title))
                }

                cardHeaderDescription {
                    text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.KeysOnThisGuild.ActiveKeys(guildPremiumKeys.size)))
                }
            }
        }

        if (guildPremiumKeys.isNotEmpty()) {
            div(classes = "cards") {
                for (key in guildPremiumKeys) {
                    guildPremiumKeyCard(
                        i18nContext,
                        guild,
                        session,
                        key,
                        now,
                        true
                    )
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }

    hr {}

    val guildPremiumKeysIds = guildPremiumKeys.map { it[DonationKeys.id] }
    val userPremiumKeysExcludingEnabledKeys = userPremiumKeys.filter { it[DonationKeys.id] !in guildPremiumKeysIds }

    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserKeys.Title))
                }

                cardHeaderDescription {
                    text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.UserKeys.Keys(userPremiumKeysExcludingEnabledKeys.size)))
                }
            }
        }

        if (userPremiumKeysExcludingEnabledKeys.isNotEmpty()) {
            div(classes = "cards") {
                for (key in userPremiumKeysExcludingEnabledKeys) {
                    guildPremiumKeyCard(
                        i18nContext,
                        guild,
                        session,
                        key,
                        now,
                        false
                    )
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }
}

fun FlowContent.guildPremiumKeyCard(
    i18nContext: I18nContext,
    guild: Guild,
    session: UserSession,
    key: ResultRow,
    now: Long,
    enabledOnThisGuild: Boolean,
) {
    div(classes = "card") {
        style = "flex-direction: row; align-items: center; gap: 0.5em;"

        div {
            style = "flex-grow: 1; display: flex; gap: 0.5em; align-items: center;"

            img {
                if (enabledOnThisGuild) {
                    style = "width: 48px; height: 48px; border-radius: 25%;"
                    src = guild.iconUrl ?: ""
                } else {
                    style = "width: 48px; height: 48px; border-radius: 99999px;"
                    src = session.getEffectiveAvatarUrl()
                }
            }

            div {
                style = "display: flex; flex-direction: column;"
                b {
                    text("Key (R$ ${key[DonationKeys.value]})")

                    if (!enabledOnThisGuild && key[DonationKeys.activeIn] != null) {
                        text(" ")
                        div(classes = "tag warn") {
                            text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.ActivatedOnAnotherServer))
                        }
                    }
                }

                div {
                    text("Expirará em ${DateUtils.formatDateDiff(i18nContext, now, key[DonationKeys.expiresAt])}")
                }
            }
        }

        div {
            style = "display: grid;grid-template-columns: 1fr;grid-column-gap: 0.5em;"

            if (enabledOnThisGuild) {
                discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                    attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/premium-keys/${key[DonationKeys.id]}/deactivate"
                    attributes["bliss-swap:200"] = "body (innerHTML) -> #section-config (innerHTML)"
                    attributes["bliss-indicator"] = "this"

                    text("Desativar")
                }
            } else {
                discordButton(ButtonStyle.PRIMARY) {
                    attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/premium-keys/${key[DonationKeys.id]}/activate"
                    attributes["bliss-swap:200"] = "body (innerHTML) -> #section-config (innerHTML)"
                    attributes["bliss-indicator"] = "this"

                    text("Ativar")
                }
            }
        }
    }
}