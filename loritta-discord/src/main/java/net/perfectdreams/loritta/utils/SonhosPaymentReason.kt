package net.perfectdreams.loritta.utils

enum class SonhosPaymentReason(val overrideLocaleName: String? = null, val multipleUsersRequired: Boolean = false) {
	UNKNOWN,

	PAYMENT(multipleUsersRequired = true),
	PAYMENT_TAX("commands.economy.transactions.sentMoneySonhosTax"),
	BOM_DIA_E_CIA,
	PROFILE,
	SHIP_EFFECT,
	MARRIAGE,
	MARRIAGE_TAX,
	DAILY,
	NITRO_BOOST_BONUS,
	RAFFLE,
	GARTICOS_TRANSFER,
	BACKGROUND,
	SPARKLYPOWER,
	INACTIVE_DAILY_TAX,
	COIN_FLIP_BET(multipleUsersRequired = true),
	STOCKS,
	GUESS_NUMBER,
	EMOJI_FIGHT
}