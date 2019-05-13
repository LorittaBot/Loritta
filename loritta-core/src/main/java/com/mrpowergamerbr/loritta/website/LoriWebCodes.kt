package com.mrpowergamerbr.loritta.website

@Deprecated(message = "Use LoriWebCode")
object LoriWebCodes {
	const val SUCCESS = 0 // sucesso
	const val RATE_LIMITED = 1 // rate limited ao acessar a API
	const val TRYING_TO_SAVE_PARTNER_CONFIG_WHILE_NOT_PARTNER = 2 // unused
	const val UNKNOWN_GUILD = 3 // a guild não existe
	const val UNAUTHORIZED = 4 // não autorizado (deslogado no Discord ou sem permissão para acessar a guild)
	const val ALREADY_VOTED_TODAY = 5 // já votou hoje
	const val NOT_IN_GUILD = 6 // não está na guild
	const val INVALID_CAPTCHA_RESPONSE = 7 // captcha inválida
	const val MISSING_PAYLOAD_HANDLER = 8 // faltando payload handler para a config especificada
	const val BAD_IP = 9 // IP foi bloqueado devido a spam
	const val NOT_VERIFIED = 10 // conta não verificada (Discord)
	const val BAD_EMAIL = 11 // email usado para spam
	const val MISSING_PERMISSION = 12 // quando falta alguma permissão
	const val ALREADY_IN_GUILD = 13 // quando o usuário já está na guild
	const val INSUFFICIENT_FUNDS = 14 // usuário não possui dinheiro suficiente
	const val NOT_IN_SERVER_LIST = 15 // quando o servidor não está na lista de servidores
	const val DISABLED_DIRECT_MESSAGES = 16

	// TIC-TAC-TOE
	const val INVALID_ROOM = 101
	const val MISSING_ROOM_ID = 102
	const val ROOM_IS_FULL = 103
	const val ALREADY_INSIDE_THIS_ROOM = 104
	const val MOVEMENT_DONE = 105
}