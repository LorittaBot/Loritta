package net.perfectdreams.loritta.morenitta.profile.profiles

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.utils.makeRoundedCorners
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.awt.Color
import java.awt.image.BufferedImage

class DebugProfileCreator(loritta: LorittaBot) : StaticProfileCreator(loritta, "debug") {
	override suspend fun create(
        sender: ProfileUserInfoData,
        user: ProfileUserInfoData,
        userProfile: Profile,
        guild: ProfileGuildInfoData?,
        badges: List<BufferedImage>,
        locale: BaseLocale,
        i18nContext: I18nContext,
        background: BufferedImage,
        aboutMe: String,
        allowedDiscordEmojis: List<Long>?
	): BufferedImage {
		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		graphics.color = Color.WHITE

		graphics.fillRect(0, 0, 800, 600)
		graphics.color = Color.BLACK

		graphics.drawString("Perfil de $user", 20, 20)
		graphics.drawString("Apenas para Testes!!!", 400, 400)

		return base.makeRoundedCorners(15)
	}
}