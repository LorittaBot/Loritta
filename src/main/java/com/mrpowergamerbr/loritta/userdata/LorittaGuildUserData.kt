package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.utils.MiscUtils
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
	var warns = mutableListOf<ModerationConfig.Warn>()

	@BsonIgnore
	fun getCurrentLevel(): LorittaProfile.XpWrapper {
		return LorittaProfile.XpWrapper((xp / 1000).toInt(), xp)
	}

	@BsonIgnore
	fun getExpToAdvanceFrom(lvl: Int): Int {
		return lvl * 1000
	}
}