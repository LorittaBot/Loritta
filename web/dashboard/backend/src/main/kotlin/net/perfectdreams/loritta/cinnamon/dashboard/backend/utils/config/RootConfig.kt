package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class RootConfig(
    val sessionHex: String,
    val sessionName: String,
    val sessionDomain: String,
    val lorittaMainRpcUrl: String,
    val legacyDashboardUrl: String,
    val spicyMorenittaJsPath: String?,
    val enableCORS: Boolean,
    val perfectPayments: PerfectPaymentsConfig,
    val userAuthenticationOverride: UserAuthenticationOverrideConfig,
    val pudding: PuddingConfig,
    val authorizationTokens: List<AuthorizationToken>
)