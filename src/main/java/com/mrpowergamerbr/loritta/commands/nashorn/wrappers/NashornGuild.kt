package com.mrpowergamerbr.loritta.commands.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.entities.Guild

/**
 * Wrapper para a Guild, usado para imagens de comandos Nashorn
 */
class NashornGuild(private val context: CommandContext, private val guild: Guild) {
	fun getName(): String {
		return guild.name
	}

	fun getIconUrl(): String {
		return guild.iconUrl
	}

	fun getIcon(): NashornImage? {
		val image = LorittaUtils.downloadImage(guild.iconUrl) ?: return null;
		return NashornImage(image)
	}

	fun getMembers(): MutableList<NashornLorittaUser> {
		val members = mutableListOf<NashornLorittaUser>()

		guild.members.forEach {
			members.add(NashornLorittaUser(it, context.config.userData.getOrDefault(it.user.id, LorittaServerUserData())))
		}

		return members
	}

	fun getRoles(): MutableList<NashornRole> {
		val roles = mutableListOf<NashornRole>()

		guild.roles.forEach {
			roles.add(NashornRole(it))
		}

		return roles
	}

	fun getRoleById(id: String): NashornRole {
		return NashornRole(guild.getRoleById(id))
	}

	fun getMemberById(id: String): NashornLorittaUser {
		return NashornLorittaUser(guild.getMemberById(id), context.config.userData.getOrDefault(id, LorittaServerUserData()));
	}

	fun play(url: String) {
		if (context.config.musicConfig.isEnabled) {
			loritta.loadAndPlay(context, url)
		}
	}

	fun ban(user: NashornUser, delDays: Int, reason: String) {
		guild.controller.ban(user.user, delDays, reason).complete()
	}

	fun kick(member: NashornMember, reason: String) {
		guild.controller.kick(member.member, reason).complete()
	}

	fun unban(id: String) {
		guild.controller.unban(id).complete()
	}
}