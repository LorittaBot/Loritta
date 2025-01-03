package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi

import net.perfectdreams.loritta.common.utils.TokenType

data class TokenInfo(
    val token: String,
    val tokenType: TokenType,
    val creatorId: Long,
    val userId: Long
)