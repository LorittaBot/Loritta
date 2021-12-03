package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserSetting
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import org.jetbrains.exposed.sql.update

class PuddingUserSetting(
    private val pudding: Pudding,
    val data: UserSetting
) {
    companion object;

    val id by data::id
    val aboutMe by data::aboutMe
    val gender by data::gender

    suspend fun setGender(gender: Gender) = pudding.transaction {
        UserSettings.update({ UserSettings.id eq this@PuddingUserSetting.id.value.toLong() }) {
            it[UserSettings.gender] = gender
        }
    }
}