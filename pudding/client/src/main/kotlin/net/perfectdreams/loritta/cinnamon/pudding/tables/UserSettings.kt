package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.utils.Gender
import org.jetbrains.exposed.dao.id.LongIdTable

object UserSettings : LongIdTable() {
    val aboutMe = text("about_me").nullable()
    val gender = enumeration("gender", Gender::class)
    val activeProfileDesign = optReference("active_profile_design", ProfileDesigns)
    val activeBackground = optReference("active_background", Backgrounds)
    val doNotSendXpNotificationsInDm = bool("do_not_send_xp_notifications_in_dm").default(false)
    val discordAccountFlags = integer("discord_account_flags").default(0)
    val discordPremiumType = integer("discord_premium_type").nullable()
    var language = text("language").nullable()
}