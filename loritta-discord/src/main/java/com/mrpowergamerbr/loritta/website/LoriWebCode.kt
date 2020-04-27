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
	BANNED(704, "Banned"),
	INVALID_NUMBER(710, "Invalid Number"),
	INSUFFICIENT_FUNDS(711, "Insufficient Funds"),
	ITEM_NOT_FOUND(712, "Item not found"),
	COOLDOWN(713, "Can't act now, on cooldown"),
	ALREADY_BOUGHT_THE_ITEM(714, "You already have the item"),
	INVALID_IMAGE_RESOLUTION(715, "Invalid Image Resolution"),
	MEMBER_NOT_IN_GUILD(720, "Member is not in provided guild"),
	MEMBER_DISABLED_DIRECT_MESSAGES(721, "Member disabled direct messages"),
	CHANNEL_DOESNT_EXIST(722, "Channel doesn't exist"),
	CANT_TALK_IN_CHANNEL(723, "Can't talk in channel"),
	INVALID_MESSAGE(724, "Invalid message"),
	MESSAGE_DOESNT_EXIST(725, "Message doesn't exist"),
	ALREADY_GOT_THE_DAILY_REWARD_SAME_ACCOUNT_TODAY(730, "You already got the daily reward today!"),
	ALREADY_GOT_THE_DAILY_REWARD_SAME_IP_TODAY(731, "You already got the daily reward today in another account!"),
	BLACKLISTED_IP(732, "IP is blacklisted"),
	UNVERIFIED_ACCOUNT(733, "Your account is unverified!"),
	BLACKLISTED_EMAIL(734, "Email is blacklisted"),
	MFA_DISABLED(735, "Your account does not have MFA enabled!"),

	RATE_LIMIT(799, "Rate limited!"),

	MISSING_PAYLOAD_HANDLER(810, "Missing Payload Handler");

	fun fromErrorId(id: Int) = values().first { it.errorId == errorId }
}