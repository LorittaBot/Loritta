package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.utils.Gender

@Serializable
data class ProfileSettings(
    val id: Long,
    val aboutMe: String?,
    val gender: Gender,
    val activeProfileDesign: String?,
    val activeBackground: String?,
    val doNotSendXpNotificationsInDm: Boolean,
    val discordAccountFlags: Int,
    val discordPremiumType: Int?,
    var language: String?
)