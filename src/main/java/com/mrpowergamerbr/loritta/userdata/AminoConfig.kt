package com.mrpowergamerbr.loritta.userdata

data class AminoConfig(
	var isEnabled: Boolean, // Está ativado?
	var inviteUrl: String?, // Invite URL da comunidade do Amino, usado para a Loritta entrar lá
	var communityId: String?,
	var repostToChannelId: String?, // ID do canal que a Loritta irá repostar
    var fixAminoImages: Boolean // Corrigir imagens do Amino no Discord
    ) {
	constructor() : this(false, null, null, null, false)
}