package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class RootConfig(
    val sessionHex: String,
    val sessionName: String,
    val sessionDomain: String,
    val unauthorizedRedirectUrl: String,
    val legacyDashboardUrl: String,
    val enableCORS: Boolean,
    val perfectPayments: PerfectPaymentsConfig,
    val userAuthenticationOverride: UserAuthenticationOverrideConfig,
    val pudding: PuddingConfig
)