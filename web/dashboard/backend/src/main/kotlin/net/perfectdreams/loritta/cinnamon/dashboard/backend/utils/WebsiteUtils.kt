package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

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
}