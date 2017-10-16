package com.mrpowergamerbr.loritta.userdata

data class AminoConfig(
	var isEnabled: Boolean, // Está ativado?
    var fixAminoImages: Boolean, // Corrigir imagens do Amino no Discord
	var syncAmino: Boolean,
	var aminos: MutableList<AminoInfo>
    ) {
	constructor() : this(false, false, false, mutableListOf<AminoInfo>())

	data class AminoInfo(
			var inviteUrl: String?, // Invite URL da comunidade do Amino, usado para a Loritta entrar lá
			var communityId: String?,
			var repostToChannelId: String?
	) {
		constructor() : this(null, null, null)
	}
}