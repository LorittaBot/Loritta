package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

import dev.kord.common.entity.DiscordUser

object WebsiteUtils {
    fun checkIfAccountHasMFAEnabled(discordUser: DiscordUser): VerificationResult {
        // This is a security measure, to avoid "high risk" purchases.
        // We will require that users need to verify their account + have MFA enabled.
        if (!discordUser.verified.discordBoolean)
            return VerificationResult.UnverifiedAccount

        if (!discordUser.mfaEnabled.discordBoolean)
            return VerificationResult.MultiFactorAuthenticationDisabled

        return VerificationResult.Success
    }

    sealed class VerificationResult {
        object UnverifiedAccount : VerificationResult()
        object MultiFactorAuthenticationDisabled : VerificationResult()
        object Success : VerificationResult()
    }
}