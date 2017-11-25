package com.mrpowergamerbr.loritta.userdata

/**
 * Usado para guardar informações especificas para um servidor, como XP, nicknames, etc.
 */
class LorittaServerUserData {
	var xp: Long = 0; // XP do usuário no servidor
	var isMuted: Boolean = false
	var temporaryMute: Boolean = false
	var expiresIn: Long = 0L

	fun getCurrentLevel(): LorittaProfile.XpWrapper {
		var lvl = 1;
		var currentXp = xp;
		var expToAdvance = getExpToAdvanceFrom(lvl);
		while (currentXp > expToAdvance) {
			currentXp -= expToAdvance;
			lvl++;
			expToAdvance = getExpToAdvanceFrom(lvl);
		}
		return LorittaProfile.XpWrapper(lvl, currentXp);
	}

	fun getExpToAdvanceFrom(lvl: Int): Int {
		return 325 + lvl * (25 + lvl)
	}
}