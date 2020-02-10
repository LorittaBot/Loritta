package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

enum class CommandCategory {
	FUN,
	IMAGES,
	MINECRAFT,
	POKEMON,
	UNDERTALE,
	ROBLOX,
	ANIME,
	DISCORD,
	MISC,
	ADMIN,
	UTILS,
	SOCIAL,
	ACTION,
	ECONOMY,
	MUSIC,
	FORTNITE,
	MAGIC; // Esta categoria é usada para comandos APENAS para o dono do bot

	fun getLocalizedName(locale: BaseLocale): String {
		return locale["commands.category.${this.name.toLowerCase()}.name"]
	}

	fun getLocalizedDescription(locale: BaseLocale): String {
		return locale["commands.category.${this.name.toLowerCase()}.description"]
	}
}