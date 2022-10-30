package net.perfectdreams.loritta.deviouscache.data

import kotlinx.serialization.Serializable

@Serializable
data class DeviousUserAndMember(
    val user: DeviousUserData,
    val member: DeviousMemberData,
)