package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img

@Composable
fun SupportedPaymentMethods(i18nContext: I18nContext) {
    Div(
        attrs = {
            classes("payment-methods-wrapper")
        }
    ) {
        Div(
            attrs = {
                classes("payment-methods-title")
            }
        ) {
            LocalizedText(i18nContext, I18nKeysData.Website.Dashboard.PaymentMethods.Title)
        }

        Div(
            attrs = {
                classes("payment-methods")
            }
        ) {
            PaymentMethod(
                i18nContext,
                I18nKeysData.Website.Dashboard.PaymentMethods.Pix,
                "https://payments.perfectdreams.net/assets/img/methods/pix.svg"
            )

            PaymentMethod(
                i18nContext,
                I18nKeysData.Website.Dashboard.PaymentMethods.CreditCard,
                "https://payments.perfectdreams.net/assets/img/methods/credit-card.svg"
            )

            PaymentMethod(
                i18nContext,
                I18nKeysData.Website.Dashboard.PaymentMethods.DebitCard,
                "https://payments.perfectdreams.net/assets/img/methods/debit-card.svg"
            )

            PaymentMethod(
                i18nContext,
                I18nKeysData.Website.Dashboard.PaymentMethods.BrazilBankTicket,
                "https://payments.perfectdreams.net/assets/img/methods/boleto.svg"
            )
        }
    }
}

@Composable
fun PaymentMethod(i18nContext: I18nContext, paymentName: StringI18nData, paymentImage: String) {
    Div(
        attrs = {
            classes("payment-method")
        }
    ) {
        Img(src = paymentImage)

        Div {
            LocalizedText(i18nContext, paymentName)
        }
    }
}