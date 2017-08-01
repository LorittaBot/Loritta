package com.mrpowergamerbr.loritta.commands.nashorn.wrappers

import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import net.dv8tion.jda.core.entities.Member

class NashornLorittaUser(private val backedMember: Member, private val userData: LorittaServerUserData) : NashornMember(backedMember) {
	fun getXp(): Int {
		return userData.xp
	}

	fun getCurrentLevel(): Int {
		return userData.getCurrentLevel().currentLevel
	}
}