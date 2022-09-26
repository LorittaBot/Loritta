package net.perfectdreams.loritta.morenitta.profile

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.ProfileCreator
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.utils.makeRoundedCorners
import net.dv8tion.jda.api.entities.Guild
import java.awt.Color
import java.awt.image.BufferedImage

class DebugProfileCreator : ProfileCreator("debug") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
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