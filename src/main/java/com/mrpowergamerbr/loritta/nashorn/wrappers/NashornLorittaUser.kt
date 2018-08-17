package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.userdata.LorittaGuildUserData
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.entities.Member

class NashornLorittaUser(private val backedMember: Member, private val userData: LorittaGuildUserData, private val serverConfig: ServerConfig) : NashornMember(backedMember) {
	@NashornCommand.NashornDocs()
	fun getXp(): Long {
		return userData.xp
	}

	@NashornCommand.NashornDocs()
	fun getCurrentLevel(): Int {
		return userData.getCurrentLevel().currentLevel
	}

	@NashornCommand.NashornDocs()
	fun setXp(newXp: Long) {
		userData.xp = newXp
		loritta save serverConfig
	}
}