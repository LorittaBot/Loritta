package com.mrpowergamerbr.loritta.commands

enum class CommandCategory constructor(val fancyTitle: String, var description: String) {
	FUN("CommandCategory_FUN_Name", "CommandCategory_FUN_Description"),
	IMAGES("CommandCategory_IMAGES_Name", "CommandCategory_IMAGES_Description"),
	MINECRAFT("CommandCategory_MINECRAFT_Name", "CommandCategory_MINECRAFT_Description"),
	POKEMON("CommandCategory_POKEMON_Name", "CommandCategory_POKEMON_Description"),
	UNDERTALE("CommandCategory_UNDERTALE_Name", "CommandCategory_UNDERTALE_Description"),
	ROBLOX("CommandCategory_ROBLOX_Name", "CommandCategory_ROBLOX_Description"),
	ANIME("CommandCategory_ANIME_Name", "CommandCategory_ANIME_Description"),
	DISCORD("CommandCategory_DISCORD_Name", "CommandCategory_DISCORD_Description"),
	MISC("CommandCategory_MISC_Name", "CommandCategory_MISC_Description"),
	ADMIN("CommandCategory_ADMIN_Name", "CommandCategory_ADMIN_Description"),
	UTILS("CommandCategory_UTILS_Name", "CommandCategory_UTILS_Description"),
	SOCIAL("CommandCategory_SOCIAL_Name", "CommandCategory_SOCIAL_Description"),
	ECONOMY("CommandCategory_ECONOMY_Name", "CommandCategory_ECONOMY_Description"),
	MUSIC("CommandCategory_MUSIC_Name", "CommandCategory_MUSIC_Description"),
	MAGIC("CommandCategory_MAGIC_Name", "CommandCategory_MAGIC_Description") // Esta categoria é usada para comandos APENAS para o dono do bot
}