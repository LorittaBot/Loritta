package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.unsafe
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData

fun FlowContent.paymentHeroWrapper(i18nContext: I18nContext) {
    div(classes = "payment-hero-wrapper") {
        div(classes = "hero-web-animation") {
            unsafe {
                raw(
                    LorittaWebsite::class.java.getResourceAsStream("/website/animations/loritta-sonhos.html")
                        .readAllBytes().toString(Charsets.UTF_8)
                )
            }
        }

        div(classes = "payment-methods-wrapper") {
            div(classes = "payment-methods-title") {
                text(i18nContext.get(DashboardI18nKeysData.PaymentMethods.Title))
            }

            div(classes = "payment-methods") {
                div(classes = "payment-method") {
                    img(src = "https://payments.perfectdreams.net/assets/img/methods/pix.svg") {}
                    text(i18nContext.get(DashboardI18nKeysData.PaymentMethods.Pix))
                }

                div(classes = "payment-method") {
                    img(src = "https://payments.perfectdreams.net/assets/img/methods/credit-card.svg") {}
                    text(i18nContext.get(DashboardI18nKeysData.PaymentMethods.CreditCard))
                }

                div(classes = "payment-method") {
                    img(src = "https://payments.perfectdreams.net/assets/img/methods/debit-card.svg") {}
                    text(i18nContext.get(DashboardI18nKeysData.PaymentMethods.DebitCard))
                }

                div(classes = "payment-method") {
                    img(src = "https://payments.perfectdreams.net/assets/img/methods/boleto.svg") {}
                    text(i18nContext.get(DashboardI18nKeysData.PaymentMethods.BrazilBankTicket))
                }
            }
        }
    }
}