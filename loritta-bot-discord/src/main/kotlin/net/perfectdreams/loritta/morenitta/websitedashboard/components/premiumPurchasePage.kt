package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons

data class PremiumPlanColumn(
    val name: String,
    val planValue: Int,
    val monthlyPriceCents: Long,
    val highlight: Boolean
)

fun DIV.premiumBillingToggle(i18nContext: I18nContext) {
    // Hidden radio inputs
    radioInput {
        id = "billing-monthly"
        classes = setOf("billing-toggle-radio")
        name = "billing-period"
        checked = true
    }
    radioInput {
        id = "billing-annual"
        classes = setOf("billing-toggle-radio")
        name = "billing-period"
    }

    // Toggle pill
    div(classes = "premium-billing-toggle-wrapper") {
        div(classes = "premium-billing-toggle") {
            label(classes = "billing-option") {
                htmlFor = "billing-monthly"
                text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.Monthly))
            }
            label(classes = "billing-option") {
                htmlFor = "billing-annual"
                text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.Annual))
                span(classes = "billing-discount-badge") {
                    text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.XDiscount(20)))
                }
            }
        }
    }
}

fun DIV.premiumPlanCards(i18nContext: I18nContext, plans: List<PremiumPlanColumn>, buyUrl: String) {
    div(classes = "premium-plans-wrapper") {
        for (plan in plans) {
            val discountedMonthlyCents = (plan.monthlyPriceCents * 0.8).toLong()
            val fullTotalCents = plan.monthlyPriceCents * 12
            val discountedTotalCents = (plan.monthlyPriceCents * 12 * 0.8).toLong()

            div(classes = "premium-plan-card" + if (plan.highlight) " highlighted" else "") {
                if (plan.highlight) {
                    div(classes = "premium-plan-popular-badge") {
                        text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.Recommended))
                    }
                }

                div(classes = "premium-plan-header" + if (plan.highlight) " has-badge" else "") {
                    h3(classes = "premium-plan-name") {
                        text(plan.name)
                    }
                }

                // Monthly content
                div(classes = "billing-monthly-content") {
                    div(classes = "premium-plan-price") {
                        span(classes = "premium-plan-price-value") {
                            text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.FormattedPrice(plan.monthlyPriceCents / 100.0)))
                        }
                        span(classes = "premium-plan-price-period") {
                            text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.PerMonth))
                        }
                    }

                    discordButton(if (plan.highlight) ButtonStyle.SUCCESS else ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                        attributes["style"] = "width: 100%;"
                        attributes["bliss-post"] = buyUrl
                        attributes["bliss-vals-json"] = """{"planValue":${plan.planValue},"durationDays":30}"""

                        text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.BuyButton(plan.monthlyPriceCents / 100.0)))
                    }
                }

                // Annual content
                div(classes = "billing-annual-content") {
                    div(classes = "premium-plan-price") {
                        span(classes = "premium-plan-price-value") {
                            text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.FormattedPrice(discountedMonthlyCents / 100.0)))
                        }
                        span(classes = "premium-plan-price-period") {
                            text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.PerMonth))
                        }
                    }

                    p(classes = "premium-plan-price-comparison") {
                        val savingsCents = fullTotalCents - discountedTotalCents
                        text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.SavingsMessageBefore))
                        b {
                            text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.FormattedPrice(savingsCents / 100.0)))
                        }
                        text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.SavingsMessageAfter))
                    }

                    discordButton(if (plan.highlight) ButtonStyle.SUCCESS else ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                        attributes["style"] = "width: 100%;"
                        attributes["bliss-post"] = buyUrl
                        attributes["bliss-vals-json"] = """{"planValue":${plan.planValue},"durationYears":1}"""

                        text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.BuyButton(discountedTotalCents / 100.0)))
                    }
                }
            }
        }
    }
}

fun TABLE.premiumFeatureTableHead(i18nContext: I18nContext, plans: List<PremiumPlanColumn>) {
    thead {
        tr {
            th {
                style = "padding: 0.75em; text-align: left; max-width: 300px;"
                text(i18nContext.get(DashboardI18nKeysData.PremiumKeys.Billing.FeatureColumnHeader))
            }
            for (plan in plans) {
                th {
                    style = "padding: 0.75em;" + if (plan.highlight) " background: var(--loritta-blue); color: white; border-radius: 8px 8px 0 0;" else ""
                    span {
                        style = "display: block; font-size: 1.25em; font-weight: bold;"
                        text(plan.name)
                    }
                }
            }
        }
    }
}

fun <T> TBODY.premiumFeatureRow(label: String, plans: List<T>, highlight: (T) -> Boolean, check: (T) -> Boolean) {
    tr {
        td {
            style = "padding: 0.75em; text-align: left; max-width: 300px;"
            text(label)
        }
        for (plan in plans) {
            td {
                style = "padding: 0.75em;" + if (highlight(plan)) " background: rgba(88, 101, 242, 0.05);" else ""
                if (check(plan)) svgIcon(SVGIcons.Check) else svgIcon(SVGIcons.X)
            }
        }
    }
}

fun <T> TBODY.premiumValueRow(label: String, plans: List<T>, highlight: (T) -> Boolean, value: (T) -> String) {
    tr {
        td {
            style = "padding: 0.75em; text-align: left; max-width: 300px;"
            text(label)
        }
        for (plan in plans) {
            td {
                style = "padding: 0.75em;" + if (highlight(plan)) " background: rgba(88, 101, 242, 0.05);" else ""
                text(value(plan))
            }
        }
    }
}
