package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import dev.kord.common.Color
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import net.perfectdreams.randomroleplaypictures.common.Gender
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureResponse

data class RoleplayActionAttributes(
    val actionBlock: suspend RandomRoleplayPicturesClient.(Gender, Gender) -> PictureResponse,
    val retributionButtonDeclaration: ButtonClickExecutorDeclaration,
    val embedResponse: (String, String) -> StringI18nData,
    val embedColor: Color,
    val embedEmoji: Emote
)