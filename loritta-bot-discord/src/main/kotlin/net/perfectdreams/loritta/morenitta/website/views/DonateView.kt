package net.perfectdreams.loritta.morenitta.website.views

import com.google.gson.JsonArray
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.sweetmorenitta.utils.imgSrcSet
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class DonateView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    val userIdentification: DiscordLoginUserDashboardRoute.UserIdentification?,
    val keys: JsonArray
) : NavbarView(
    loritta,
    i18nContext,
    locale,
    path
) {
    companion object {
        const val LOCALE_PREFIX = "website.donate"
    }

    val rewards = listOf(
        DonationReward("ignore_me", 0.0, false),
        DonationReward("ignore_me", 99.99, false),

        // ===[ RECOMMENDED ]===
        DonationReward("Não pagar taxas em apostas", 39.99, false),
        DonationReward("Poder colocar um emoji personalizados no Emoji Fight", 39.99, false),
        DonationReward(locale["${LOCALE_PREFIX}.rewards.exclusiveProfileBadge"], 39.99, false),
        DonationReward(locale["${LOCALE_PREFIX}.rewards.customProfileBackground"], 39.99, false),
        // DonationReward("Personalizar nome/avatar da Loritta nas notificações do YouTube/Twitch/Twitter", 39.99, false),
        // Removed because this needs to be revamped
        // DonationReward(locale["${LOCALE_PREFIX}.rewards.reducedCooldown"], 39.99, false),

        // ===[  COMPLETE  ]===

        // ===[  NUMBERS  ]===
        DonationReward(
            locale["${LOCALE_PREFIX}.rewards.everyMinuteSonhos"],
            39.99,
            false,
            callback = { column ->
                when {
                    column >= 99.99 -> +"10"
                    column >= 39.99 -> +"4"
                    column >= 19.99 -> +"2"
                    else -> +"0"
                }
            }),
        DonationReward(
            locale["${LOCALE_PREFIX}.rewards.dailyMultiplier"],
            19.99,
            false,
            callback = { column ->
                +(ServerPremiumPlans.getPlanFromValue(column).dailyMultiplier.toString() + "x")
            }),
        DonationReward(
            locale["${LOCALE_PREFIX}.rewards.maxLevelUpRoles"],
            19.99,
            false,
            callback = { column ->
                +ServerPremiumPlans.getPlanFromValue(column).maxLevelUpRoles.toString()
            }),
        DonationReward(
            locale["${LOCALE_PREFIX}.rewards.maxMemberCounters"],
            19.99,
            false,
            callback = { column ->
                +ServerPremiumPlans.getPlanFromValue(column).memberCounterCount.toString()
            }),
        DonationReward(
            i18nContext.get(I18nKeysData.Website.Donate.Rewards.TwitchPremiumTrack),
            19.99,
            false,
            callback = { column ->
                +ServerPremiumPlans.getPlanFromValue(column).maxUnauthorizedTwitchChannels.toString()
            }),
        DonationReward(
            i18nContext.get(I18nKeysData.Website.Donate.Rewards.YouTubeChannelTrack),
            19.99,
            false,
            callback = { column ->
                +ServerPremiumPlans.getPlanFromValue(column).maxYouTubeChannels.toString()
            }),
        DonationReward(
            locale["${LOCALE_PREFIX}.rewards.maxDailyLimit"],
            39.99,
            false,
            callback = { column ->
                +UserPremiumPlans.getPlanFromValue(column).maxDreamsInDaily.toString()
            }),
        DonationReward(
            locale["${LOCALE_PREFIX}.rewards.giveBackRepChange"],
            39.99,
            false,
            callback = { column ->
                +(UserPremiumPlans.getPlanFromValue(column).loriReputationRetribution.toString() + "%")
            }),
        // Removed because Global XP is a bit worthless nowadays
        /* DonationReward(
            locale["${LOCALE_PREFIX}.rewards.globalExperienceMultiplier"],
            99.99,
            false,
            callback = { column ->
                +(ServerPremiumPlans.getPlanFromValue(column).globalXpMultiplier.toString() + "x")
            }) */
    )

    override fun getTitle() = locale["website.donate.title"]

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            div {
                style = "text-align: center;"
                h1(classes = "sectionHeader") {
                    style = "font-size: 50px;"
                    + locale["website.donate.needYourHelp"]
                }

                h2(classes = "sectionHeader") {
                    style = "font-size: 30px;"
                    + locale["website.donate.stayAwesome"]
                }
            }

            div(classes = "media") {
                div(classes = "media-figure") {
                    imgSrcSet(
                        "${websiteUrl}/v2/assets/img/donate/",
                        "lori_donate.png",
                        "(max-width: 800px) 50vw, 15vw",
                        1272,
                        272,
                        100
                    )
                    // img(src = "${websiteUrl}/assets/img/loritta_pobre.png", alt = "Loritta Pobre") {}
                }

                div(classes = "media-body") {
                    h2(classes = "sectionHeader") {
                        + locale["website.donate.title"]
                    }

                    for (text in locale.getList("website.donate.introDonate")) {
                        p {
                            unsafe {
                                + text
                            }
                        }
                    }

                    div {
                        style = "text-align: center;"

                        a(href = "#plans-features") {
                            div(classes = "button-discord button-discord-info pure-button") {
                                style = "font-size: 1.5em;"

                                i(classes = "fas fa-list") {}
                                +" ${locale["website.donate.viewPlans"]}"
                            }
                        }

                        if (keys.size() != 0) {
                            div(classes = "button-discord button-discord-info pure-button") {
                                id = "renew-button"
                                style = "font-size: 1.5em;"

                                i(classes = "fas fa-sync-alt") {}
                                + " ${locale["website.donate.renewPlan"]}"
                            }
                        }
                    }

                    div {
                        style = "text-align: center; margin: 8px;"

                        video {
                            controls = true
                            width = "400"
                            source {
                                src = "https://stuff.loritta.website/premium/lori-sem-dinheiro.mp4"
                                type = "video/mp4"
                            }
                            + "Your browser does not support HTML5 video."
                        }
                    }
                }
            }
        }

        div(classes = "even-wrapper wobbly-bg") {
            div(classes = "media") {
                div(classes = "vertically-centered-content") {
                    style = "max-width: 100%;"
                    div(classes = "sectionText") {
                        div {
                            style = "text-align: center;"
                            h2(classes = "sectionHeader") {
                                + locale["website.donate.donationBenefits"]
                            }

                            p {
                                + locale["website.donate.benefitsExplain"]
                            }

                            p {
                                a(href = "/sponsors", target = "_blank") {
                                    + locale["website.donate.benefitsSponsor"]
                                }
                            }
                        }

                        div(classes = "sectionText") {
                            div {
                                style = "text-align: center;"
                                h2(classes = "sectionHeader") {
                                    + "Nossos Planos"
                                }
                            }
                        }

                        div {
                            id = "plans-features"
                            style = "margin: 0 auto"

                            table(classes = "fancy-table centered-text") {
                                style = "margin: 0 auto;"

                                val rewardColumn = mutableListOf<Double>()
                                tr {
                                    th { +"" }
                                    rewards.asSequence()
                                        .map { it.minimumDonation }
                                        .distinct()
                                        .filter { it == 0.0 || it == 19.99 || it == 39.99 || it == 99.99 }
                                        .sortedBy { it }.toList().forEach {
                                            th {
                                                val titlePrefix = when (it) {
                                                    0.0 -> locale["${LOCALE_PREFIX}.plans.free"]
                                                    19.99 -> locale["${LOCALE_PREFIX}.plans.essential"]
                                                    39.99 -> locale["${LOCALE_PREFIX}.plans.recommended"]
                                                    99.99 -> locale["${LOCALE_PREFIX}.plans.complete"]
                                                    else -> "???"
                                                }

                                                if (it == 0.0) {
                                                    style = "opacity: 0.7; font-size: 0.9em;"
                                                }

                                                if (it == 39.99) {
                                                    style = "background-color: #83ff836b; font-size: 1.3em;"
                                                }

                                                if (it != 0.0) {
                                                    text("$titlePrefix (R$" + it.toString().replace(".", ",") + ")")
                                                } else {
                                                    text(titlePrefix)
                                                }
                                            }
                                            rewardColumn.add(it)
                                        }
                                }

                                for (reward in rewards.filterNot { it.doNotDisplayInPlans }
                                    .filter { it.name != "ignore_me" }) {
                                    tr {
                                        td {
                                            attributes["style"] = "font-weight: 800;"
                                            +reward.name
                                        }
                                        for (column in rewardColumn) {
                                            td {
                                                if (column == 0.0) {
                                                    style = "opacity: 0.7; font-size: 0.8em;"
                                                }

                                                if (column == 39.99) {
                                                    style = "background-color: #83ff836b;"
                                                }
                                                reward.callback.invoke(this, column)
                                            }
                                        }
                                    }
                                }

                                tr {
                                    // =====[ PREMIUM PLANS ]=====
                                    td {
                                        +""
                                    }

                                    val needsToLogin = userIdentification == null
                                    val url = "https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fdashboard&scope=identify%20guilds%20email&response_type=code&client_id=297153970613387264"

                                    td {
                                    }

                                    fun TD.createBuyPlanButton(buttonPlanId: String, isBigger: Boolean) {
                                        if (isBigger)
                                            style = "background-color: #83ff836b;"

                                        if (needsToLogin) {
                                            a(href = url) {
                                                div(classes = "button-discord button-discord-info pure-button") {
                                                    style = if (isBigger)
                                                        "display: flex; align-items: center; gap: 0.5em; font-size: 1.2em;"
                                                    else
                                                        "display: flex; align-items: center; gap: 0.5em;"

                                                    i("fas fa-gift") {}
                                                    +" ${locale["${LOCALE_PREFIX}.buyPlan"]}"
                                                }
                                            }
                                        } else {
                                            div(classes = "button-discord button-discord-info pure-button") {
                                                id = buttonPlanId
                                                style = if (isBigger)
                                                    "display: flex; align-items: center; gap: 0.5em; font-size: 1.2em;"
                                                else
                                                    "display: flex; align-items: center; gap: 0.5em;"

                                                i("fas fa-gift") {}
                                                +" ${locale["${LOCALE_PREFIX}.buyPlan"]}"
                                            }
                                        }
                                    }

                                    td {
                                        createBuyPlanButton("donate-button-plan1", false)
                                    }

                                    td {
                                        createBuyPlanButton("donate-button-plan2", true)
                                    }

                                    td {
                                        createBuyPlanButton("donate-button-plan3", false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        div(classes = "odd-wrapper wobbly-bg") {
            div(classes = "content-wrapper") {
                style = "max-width: 100%;"
                div(classes = "sectionText") {
                    id = "top-donators-scoreboard-wrapper"
                    style = "text-align: center;"

                    h2(classes = "sectionHeader") {
                        + locale["website.donate.thanksToEveryone"]
                    }

                    /* img(src = "https://loritta.website/assets/img/loritta_pudim.png", alt = "Loritta com um pudim na mão", classes = "animate-on-scroll-up is-invisible") {
                        height = "300"
                    } */
                }
            }
        }

        div {
            id = "donation-keys-json"
            style = "display: none;"

            unsafe {
                + keys.toString()
            }
        }
    }

    inner class DonationReward(val name: String, val minimumDonation: Double, val doNotDisplayInPlans: Boolean, val callback: TD.(Double) -> Unit = { column ->
        if (column >= minimumDonation) {
            i("fas fa-check") {}
        } else {
            i("fas fa-times") {}
        }
    })
}