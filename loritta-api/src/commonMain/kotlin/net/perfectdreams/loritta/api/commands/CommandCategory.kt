package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.serialization.Serializable

@Serializable
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
	VIDEOS,
	FORTNITE,
	MAGIC; // Esta categoria é usada para comandos APENAS para o dono do bot

	fun getLocalizedName(locale: BaseLocale): String {
		return locale["commands.category.${this.name.toLowerCase()}.name"]
	}

	fun getLocalizedDescription(locale: BaseLocale): List<String> {
		return locale.getList("commands.category.${this.name.toLowerCase()}.description")
	}
}