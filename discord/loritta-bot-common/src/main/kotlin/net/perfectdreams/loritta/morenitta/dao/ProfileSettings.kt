package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.loritta.morenitta.tables.UserSettings
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ProfileSettings(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<ProfileSettings>(UserSettings)

	var aboutMe by UserSettings.aboutMe
	var gender by UserSettings.gender
	var activeProfileDesign by ProfileDesign optionalReferencedOn UserSettings.activeProfileDesign
	var activeProfileDesignInternalName by UserSettings.activeProfileDesign
	var activeBackgroundInternalName by UserSettings.activeBackground
	var doNotSendXpNotificationsInDm by UserSettings.doNotSendXpNotificationsInDm
	var discordAccountFlags by UserSettings.discordAccountFlags
	var discordPremiumType by UserSettings.discordPremiumType
	var language by UserSettings.language
}