package com.mrpowergamerbr.loritta.profile

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import java.awt.image.BufferedImage

abstract class ProfileCreator(
		val internalName: String
) {
	abstract suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage

	open suspend fun createGif(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): List<BufferedImage> {
		return listOf(create(sender, user, userProfile, guild, badges, locale, background, aboutMe, member))
	}
}