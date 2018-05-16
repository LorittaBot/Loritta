package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class AminoConfig {
	var fixAminoImages: Boolean = false // Corrigir imagens do Amino no Discord
	var syncAmino: Boolean = false
	var aminos: MutableList<AminoInfo> = mutableListOf()

	data class AminoInfo @BsonCreator constructor(
			@BsonProperty("inviteUrl")
			var inviteUrl: String?, // Invite URL da comunidade do Amino, usado para a Loritta entrar lá
			@BsonProperty("communityId")
			var communityId: String?,
			@BsonProperty("repostToChannelId")
			var repostToChannelId: String?
	)
}