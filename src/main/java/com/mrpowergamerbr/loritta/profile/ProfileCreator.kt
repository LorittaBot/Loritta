package com.mrpowergamerbr.loritta.profile

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import java.awt.image.BufferedImage

interface ProfileCreator {
	fun create(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: ServerConfig, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage
}