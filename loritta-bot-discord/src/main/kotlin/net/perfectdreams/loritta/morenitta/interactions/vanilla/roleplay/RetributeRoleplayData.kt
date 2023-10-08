package net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay

data class RetributeRoleplayData(
    val userId: Long,
    val giver: Long,
    val receiver: Long,
    val combo: Int
)