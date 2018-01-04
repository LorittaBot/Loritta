package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty

/**
 * Usado para guardar informações especificas para um servidor, como XP, nicknames, etc.
 */
class LorittaGuildUserData @BsonCreator constructor(@BsonProperty("userId") _userId: String) {
	val userId: String = _userId
	var xp: Long = 0; // XP do usuário no servidor
	var isMuted: Boolean = false
	var temporaryMute: Boolean = false
	var expiresIn: Long = 0L

	@BsonIgnore
	fun getCurrentLevel(): LorittaProfile.XpWrapper {
		var lvl = 1
		var currentXp = xp
		var expToAdvance = getExpToAdvanceFrom(lvl)
		while (currentXp > expToAdvance) {
			currentXp -= expToAdvance
			lvl++
			expToAdvance = getExpToAdvanceFrom(lvl)
		}
		return LorittaProfile.XpWrapper(lvl, currentXp)
	}

	@BsonIgnore
	fun getExpToAdvanceFrom(lvl: Int): Int {
		return 325 + lvl * (25 + lvl)
	}
}