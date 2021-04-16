package net.perfectdreams.loritta.common.utils.config

import kotlinx.serialization.Serializable

@Serializable
class LorittaConfig(
    val token: String,
    val publicKey: String,
    val applicationId: Long,
    val repositoryFolder: String
)