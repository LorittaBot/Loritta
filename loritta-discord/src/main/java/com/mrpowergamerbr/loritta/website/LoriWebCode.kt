package com.mrpowergamerbr.loritta.website

enum class LoriWebCode(val errorId: Int, val fancyName: String) {
	// 6xx - SUCCESS / INFO
	// 7xx - USER ERROR
	// 8xx - LORI ERROR
	UNAUTHORIZED(700, "Unauthorized"),
	FORBIDDEN(701, "Forbidden"),
	INVALID_RECAPTCHA(702, "Invalid reCAPTCHA"),
	UNKNOWN_GUILD(702, "Unknown Guild"),
	UNKNOWN_USER(703, "Unknown User"),
	INVALID_NUMBER(710, "Invalid Number"),
	INSUFFICIENT_FUNDS(711, "Insufficient Funds"),
	ITEM_NOT_FOUND(712, "Item not found"),
	COOLDOWN(713, "Can't act now, on cooldown"),
	MEMBER_NOT_IN_GUILD(720, "Member is not in provided guild"),
	MEMBER_DISABLED_DIRECT_MESSAGES(721, "Member disabled direct messages"),
	CHANNEL_DOESNT_EXIST(722, "Channel doesn't exist"),
	CANT_TALK_IN_CHANNEL(723, "Can't talk in channel"),
	INVALID_MESSAGE(724, "Invalid message"),
	MESSAGE_DOESNT_EXIST(725, "Message doesn't exist"),
	RATE_LIMIT(799, "Rate limited!"),

	MISSING_PAYLOAD_HANDLER(810, "Missing Payload Handler"),
}