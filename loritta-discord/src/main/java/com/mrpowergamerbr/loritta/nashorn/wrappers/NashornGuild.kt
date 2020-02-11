package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.Guild

/**
 * Wrapper para a Guild, usado para imagens de comandos Nashorn
 */
class NashornGuild(private val guild: Guild, private val serverConfig: MongoServerConfig) {
	@NashornCommand.NashornDocs()
	fun getName(): String {
		return guild.name
	}

	@NashornCommand.NashornDocs()
	fun getIconUrl(): String {
		return guild.iconUrl!!
	}

	@NashornCommand.NashornDocs()
	fun getIcon(): NashornImage? {
		val image = LorittaUtils.downloadImage(guild.iconUrl!!) ?: return null
		return NashornImage(image)
	}

	@NashornCommand.NashornDocs()
	fun getMembers(): MutableList<NashornLorittaUser> {
		val members = mutableListOf<NashornLorittaUser>()

		guild.members.forEach {
			members.add(NashornLorittaUser(it, serverConfig))
		}

		return members
	}

	@NashornCommand.NashornDocs()
	fun getRoles(): MutableList<NashornRole> {
		val roles = mutableListOf<NashornRole>()

		guild.roles.forEach {
			roles.add(NashornRole(it))
		}

		return roles
	}

	@NashornCommand.NashornDocs()
	fun getRoleById(id: String): NashornRole {
		return NashornRole(guild.getRoleById(id)!!)
	}

	@NashornCommand.NashornDocs()
	fun getMemberById(id: String): NashornLorittaUser {
		return NashornLorittaUser(guild.getMemberById(id)!!, serverConfig)
	}
}