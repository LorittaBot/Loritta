package com.mrpowergamerbr.loritta.commands.nashorn.wrappers

import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import net.dv8tion.jda.core.entities.Member

class NashornLorittaUser(private val member: Member, private val userData: LorittaServerUserData) : NashornMember(member) {
	fun getXp(): Int {
		return userData.xp
	}

	fun getCurrentLevel(): Int {
		return userData.getCurrentLevel().currentLevel
	}
}