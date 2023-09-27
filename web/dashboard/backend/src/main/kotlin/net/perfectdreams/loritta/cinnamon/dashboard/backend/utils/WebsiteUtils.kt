package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

object WebsiteUtils {
    fun checkIfAccountHasMFAEnabled(userIdentification: TemmieDiscordAuth.UserIdentification): VerificationResult {
        // This is a security measure, to avoid "high risk" purchases.
        // We will require that users need to verify their account + have MFA enabled.
        if (!userIdentification.verified)
            return VerificationResult.UnverifiedAccount

        if (userIdentification.mfaEnabled == false || userIdentification.mfaEnabled == null)
            return VerificationResult.MultiFactorAuthenticationDisabled

        return VerificationResult.Success
    }

    sealed class VerificationResult {
        object UnverifiedAccount : VerificationResult()
        object MultiFactorAuthenticationDisabled : VerificationResult()
        object Success : VerificationResult()
    }

    fun getDiscordCrawlerAuthenticationPage(): String {
        return createHTML().html {
            head {
                fun setMetaProperty(property: String, content: String) {
                    meta(content = content) { attributes["property"] = property }
                }
                title("Login • Loritta")
                setMetaProperty("og:site_name", "Loritta")
                setMetaProperty("og:title", "Painel da Loritta")
                setMetaProperty("og:description", "Meu painel de configuração, aonde você pode me configurar para deixar o seu servidor único e incrível!")
                setMetaProperty("og:image", "https://stuff.loritta.website/loritta-and-wumpus-dashboard-yafyr.png")
                setMetaProperty("og:image:width", "320")
                setMetaProperty("og:ttl", "660")
                setMetaProperty("og:image:width", "320")
                setMetaProperty("theme-color", LorittaColors.LorittaAqua.toHex())
                meta("twitter:card", "summary_large_image")
            }
            body {
                p {
                    + "Parabéns, você encontrou um easter egg!"
                }
            }
        }
    }
}