package com.mrpowergamerbr.loritta.profile

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import java.awt.image.BufferedImage

interface ProfileCreator {
	fun create(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: MongoServerConfig, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage

	fun createGif(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: MongoServerConfig, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): List<BufferedImage> {
		return listOf(create(sender, user, userProfile, guild, serverConfig, badges, locale, background, aboutMe, member))
	}
}