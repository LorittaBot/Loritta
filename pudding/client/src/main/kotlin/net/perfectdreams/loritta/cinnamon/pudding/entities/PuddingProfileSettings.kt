package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.ProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import org.jetbrains.exposed.sql.update

class PuddingProfileSettings(
    private val pudding: Pudding,
    val data: ProfileSettings
) {
    companion object;

    val id by data::id
    val aboutMe by data::aboutMe
    val gender by data::gender
    val activeProfileDesign by data::activeProfileDesign
    val activeBackground by data::activeBackground

    suspend fun setGender(gender: Gender) = pudding.transaction {
        UserSettings.update({ UserSettings.id eq this@PuddingProfileSettings.id.toLong() }) {
            it[UserSettings.gender] = gender
        }
    }

    suspend fun setAboutMe(aboutMe: String) = pudding.transaction {
        UserSettings.update({ UserSettings.id eq this@PuddingProfileSettings.id.toLong() }) {
            it[UserSettings.aboutMe] = aboutMe
        }
    }
}