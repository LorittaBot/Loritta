package com.mrpowergamerbr.loritta.userdata

/**
 * Usado para guardar informações especificas para um servidor, como XP, nicknames, etc.
 */
class LorittaServerUserData {
	var xp: Int = 0; // XP do usuário no servidor

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