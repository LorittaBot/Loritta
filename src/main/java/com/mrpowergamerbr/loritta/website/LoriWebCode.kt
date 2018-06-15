package com.mrpowergamerbr.loritta.website

enum class LoriWebCode(val errorId: Int, val fancyName: String) {
	// 6xx - SUCCESS / INFO
	// 7xx - USER ERROR
	// 8xx - LORI ERROR
	UNAUTHORIZED(700, "Unauthorized"),
	FORBIDDEN(701, "Forbidden"),
	UNKNOWN_GUILD(702, "Unknown Guild"),
	INVALID_NUMBER(710, "Invalid Number"),
	INSUFFICIENT_FUNDS(711, "Insufficient Funds"),
	MEMBER_NOT_IN_GUILD(720, "Member is not in provided guild"),
	MEMBER_DISABLED_DIRECT_MESSAGES(721, "Member disabled direct messages")
}