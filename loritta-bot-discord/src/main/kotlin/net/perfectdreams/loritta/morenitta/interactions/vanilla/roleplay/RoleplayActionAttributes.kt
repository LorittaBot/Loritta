package net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.utils.RoleplayAction
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import net.perfectdreams.randomroleplaypictures.common.Gender
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureResponse
import org.jetbrains.exposed.sql.Column
import java.awt.Color
import kotlin.reflect.KProperty0

data class RoleplayActionAttributes(
    val type: RoleplayAction,
    val userI18nDescription: StringI18nData,
    val buttonLabel: StringI18nData,
    val actionBlock: suspend RandomRoleplayPicturesClient.(Gender, Gender) -> PictureResponse,
    val embedResponse: (String, String) -> StringI18nData,
    val embedColor: Color,
    val embedEmoji: Emote,
    val givesAffinityReward: Boolean
)