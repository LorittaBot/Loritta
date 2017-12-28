package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.userdata.LorittaGuildUserData
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import net.dv8tion.jda.core.entities.Member

class NashornLorittaUser(private val backedMember: Member, private val userData: LorittaGuildUserData) : NashornMember(backedMember) {
	@NashornCommand.NashornDocs()
	fun getXp(): Long {
		return userData.xp
	}

	@NashornCommand.NashornDocs()
	fun getCurrentLevel(): Int {
		return userData.getCurrentLevel().currentLevel
	}
}