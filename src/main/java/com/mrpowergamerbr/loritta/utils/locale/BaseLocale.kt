package com.mrpowergamerbr.loritta.utils.locale

import com.mrpowergamerbr.loritta.utils.f

/**
 * Classe de localização base, por padrão em PT-BR
 *
 * Locales diferentes devem extender esta classe
 */
open class BaseLocale {
	@Transient
	var strings = mutableMapOf<String, String>()

	operator fun get(key: String, vararg arguments: Any?): String {
		return strings[key]!!.f(*arguments)
	}

	// TODO: Depreciar fields, usar HashMap
	// Generic
	lateinit var PROCESSING: String
	lateinit var INVALID_NUMBER: String
	lateinit var MINUTES_AND_SECONDS: String
	lateinit var NSFW_IMAGE: String
	lateinit var MENTION_RESPONSE: String
	lateinit var NO_PERMISSION: String
	lateinit var CANT_USE_IN_PRIVATE: String

	// Event Log
	lateinit var EVENTLOG_USER_ID: String
	lateinit var EVENTLOG_CHANNEL_CREATED: String
	lateinit var EVENTLOG_CHANNEL_NAME_UPDATED: String
	lateinit var EVENTLOG_CHANNEL_TOPIC_UPDATED: String
	lateinit var EVENTLOG_CHANNEL_POSITION_UPDATED: String
	lateinit var EVENTLOG_CHANNEL_DELETED: String

	// CommandBase.kt
	lateinit var HOW_TO_USE: String
	lateinit var EXAMPLE: String

	// ===[ COMMANDS - ADMINISTRATION ]===
	// HackBanCommand.kt
	lateinit var HACKBAN_REASON: String
	// MuteCommand.kt
	lateinit var MUTE_ROLE_NAME: String

	// SoftBanCommand.kt
	lateinit var SOFTBAN_DESCRIPTION: String
	lateinit var SOFTBAN_FAIL_MORE_THAN_SEVEN_DAYS: String
	lateinit var SOFTBAN_FAIL_LESS_THAN_ZERO_DAYS: String
	lateinit var SOFTBAN_BY: String
	lateinit var SOFTBAN_SUCCESS: String
	lateinit var SOFTBAN_NO_PERM: String

	// ===[ COMMANDS - DISCORD ]===
	// AvatarCommand.kt
	lateinit var AVATAR_DESCRIPTION: String

	// BotInfoCommand.kt
	lateinit var BOTINFO_TITLE: String

	// EmojiCommand.kt
	lateinit var EMOJI_DESCRIPTION: String

	// InviteCommand.kt
	lateinit var INVITE_DESCRIPTION: String
	lateinit var INVITE_INFO: String

	// ServerInfoCommand.kt
	lateinit var SERVERINFO_CHANNELS_TEXT: String
	lateinit var SERVERINFO_CHANNELS_VOICE: String

	// ===[ COMMANDS - FUN ]===
	// AmigosCommand.kt
	lateinit var AMIGOS_DESCRIPTION: String

	// AmizadeCommand.kt
	lateinit var AMIZADE_AMIZADE_COM: String
	lateinit var AMIZADE_ENDED: String
	lateinit var AMIZADE_NOW: String
	lateinit var AMIZADE_IS_MY: String
	lateinit var AMIZADE_BEST_FRIEND: String

	// AvaliarWaifuCommand.kt
	lateinit var RATEWAIFU_DESCRIPTION: String
	lateinit var RATEWAIFU_10: String
	lateinit var RATEWAIFU_9: String
	lateinit var RATEWAIFU_8: String
	lateinit var RATEWAIFU_7: String
	lateinit var RATEWAIFU_6: String
	lateinit var RATEWAIFU_5: String
	lateinit var RATEWAIFU_4: String
	lateinit var RATEWAIFU_3: String
	lateinit var RATEWAIFU_2: String
	lateinit var RATEWAIFU_1: String
	lateinit var RATEWAIFU_0: String
	lateinit var RATEWAIFU_IM_PERFECT: String
	lateinit var RATEWAIFU_RESULT: String

	// CaraCoroaCommand.kt
	lateinit var CARACOROA_HEADS: String
	lateinit var CARACOROA_TAILS: String

	// CepoCommand.kt
	lateinit var CEPO_DESCRIPTION: String

	// DeusCommand.kt
	lateinit var DEUS_DESCRIPTION: String

	// DeusesCommand.kt
	lateinit var DEUSES_DESCRIPTION: String

	// DiscordiaCommand.kt
	lateinit var DISCORDIA_DESCRIPTION: String

	// DrakeCommand.kt
	lateinit var DRAKE_DESCRIPTION: String

	// FraseToscaCommand.kt
	lateinit var FRASETOSCA_DESCRIPTION: String

	// GangueCommand.kt
	lateinit var GANGUE_DESCRIPTION: String

	// InverterCommand.kt
	lateinit var INVERTER_DESCRIPTION: String

	// LavaCommand.kt
	lateinit var LAVA_DESCRIPTION: String

	// Lalateinit valeversoCommand.kt
	lateinit var LAVAREVERSO_DESCRIPTION: String

	// TODO: Textos na imagem

	lateinit var VIEIRINHA_responses: List<String>

	// PerdaoCommand.kt
	lateinit var PERDAO_DESCRIPTION: String

	// PerfeitoCommand.kt
	lateinit var PERFEITO_DESCRIPTION: String

	// PrimeirasPalavrasCommand.kt
	lateinit var PRIMEIRAS_DESCRIPTION: String

	// QuadroCommand.kt
	lateinit var QUADRO_DESCRIPTION: String

	// TODO: RandomSAMCommand.kt & RandomMemeguy1997.kt

	// RazoesCommand.kt
	lateinit var RAZOES_DESCRIPTION: String

	// RollCommand.kt
	lateinit var ROLL_INVALID_NUMBER: String

	// ShipCommand.kt
	lateinit var SHIP_DESCRIPTION: String

	lateinit var SHIP_valor90: List<String>

	lateinit var SHIP_valor80: List<String>

	lateinit var SHIP_valor70: List<String>

	lateinit var SHIP_valor60: List<String>

	lateinit var SHIP_valor50: List<String>

	lateinit var SHIP_valor40: List<String>

	lateinit var SHIP_valor30: List<String>

	lateinit var SHIP_valor20: List<String>

	lateinit var SHIP_valor10: List<String>

	lateinit var SHIP_valor0: List<String>

	// TretaNewsCommand.kt
	lateinit var TRETANEWS_DESCRIPTION: String

	// TrumpCommand.kt
	lateinit var TRUMP_DESCRIPTION: String

	// YouTubeCommand.kt
	lateinit var YOUTUBE_RESULTS_FOR: String
	lateinit var YOUTUBE_COULDNT_FIND: String

	// ===[ COMMANDS - MINECRAFT ]===
	// McQueryCommand.kt
	lateinit var MCQUERY_DESCRIPTION: String
	lateinit var MCQUERY_OFFLINE: String
	// McSignCommand.kt
	lateinit var MCSIGN_DESCRIPTION: String

	// McStatusCommand.kt
	lateinit var MCSTATUS_DESCRIPTION: String
	lateinit var MCSTATUS_MOJANG_STATUS: String

	// McUUIDCommand.kt
	lateinit var MCUUID_DESCRIPTION: String
	lateinit var MCUUID_RESULT: String
	lateinit var MCUUID_INVALID: String

	// OfflineUUIDCommand.kt
	lateinit var OFFLINEUUID_DESCRIPTION: String
	lateinit var OFFLINEUUID_RESULT: String

	// ===[ COMMANDS - MISC ]===
	// AjudaCommand.kt
	lateinit var AJUDA_SENT_IN_PRIVATE: String

	// AngelCommand.kt
	lateinit var ANGEL_DESCRIPTION: String

	// EscolherCommand.kt
	lateinit var ESCOLHER_DESCRIPTION: String

	// PingCommand.kt
	lateinit var PING_DESCRIPTION: String

	// LanguageCommand.kt
	lateinit var LANGUAGE_INFO: String

	// ===[ COMMANDS - SOCIAL ]===
	// BackgroundCommand.kt
	lateinit var BACKGROUND_DESCRIPTION: String
	lateinit var BACKGROUND_CENTRAL: String
	lateinit var BACKGROUND_INFO: String
	lateinit var BACKGROUND_INVALID_IMAGE: String
	lateinit var BACKGROUND_UPDATED: String
	lateinit var BACKGROUND_EDITED: String
	lateinit var BACKGROUND_YOUR_CURRENT_BG: String
	lateinit var BACKGROUND_TEMPLATE_INFO: String

	// DiscriminatorCommand.kt
	lateinit var DISCRIM_DESCRIPTION: String
	lateinit var DISCRIM_NOBODY: String

	// RepCommand.kt
	lateinit var REP_DESCRIPTON: String
	lateinit var REP_SELF: String
	lateinit var REP_WAIT: String
	lateinit var REP_SUCCESS: String

	// SobreMimCommand.kt
	lateinit var SOBREMIM_DESCRIPTION: String
	lateinit var SOBREMIM_CHANGED: String

	// ===[ COMMANDS - MUSIC ]===
	// MusicInfoCommand.kt & PlaylistCommand.kt
	lateinit var MUSICINFO_NOMUSIC: String
	lateinit var MUSICINFO_INQUEUE: String
	lateinit var MUSICINFO_NOMUSIC_SHORT: String
	lateinit var MUSICINFO_REQUESTED_BY: String
	lateinit var MUSICINFO_LENGTH: String

	// PularCommand.kt
	lateinit var PULAR_DESCRIPTION: String
	lateinit var PULAR_MUSICSKIPPED: String

	// TocarCommand.kt
	lateinit var TOCAR_MUTED: String
	lateinit var TOCAR_CANTTALK: String
	lateinit var TOCAR_NOTINCHANNEL: String

	// VolumeCommand.kt
	lateinit var VOLUME_TOOHIGH: String
	lateinit var VOLUME_TOOLOW: String
	lateinit var VOLUME_LOWER: String
	lateinit var VOLUME_HIGHER: String
	lateinit var VOLUME_EXCEPTION: String

	// ~ generic ~
	lateinit var MUSIC_MAX: String
	lateinit var MUSIC_ADDED: String
	lateinit var MUSIC_PLAYLIST_ADDED: String
	lateinit var MUSIC_PLAYLIST_ADDED_IGNORED: String

	// ===[ COMMANDS - POKÉMON ]===
	// PokedexCommand.kt
	lateinit var POKEDEX_DESCRIPTION: String
	lateinit var POKEDEX_TYPES: String
	lateinit var POKEDEX_ADDED_IN_GEN: String
	lateinit var POKEDEX_NUMBER: String
	lateinit var POKEDEX_ABILITIES: String
	lateinit var POKEDEX_BASE_EXP: String
	lateinit var POKEDEX_EFFORT_POINTS: String
	lateinit var POKEDEX_CAPTURE_RATE: String
	lateinit var POKEDEX_BASE_HAPPINESS: String
	lateinit var POKEDEX_GROWTH_RATE: String
	lateinit var POKEDEX_TRAINING: String
	lateinit var POKEDEX_EVOLUTIONS: String

	// ===[ COMMANDS - UNDERTALE ]===
	// UndertaleBattleCommand.kt
	lateinit var UTBATTLE_INVALID: String

	// ===[ COMMANDS - UTILS ]===
	// KnowYourMemeCommand.kt
	lateinit var KYM_DESCRIPTION: String
	lateinit var KYM_COULDNT_FIND: String
	lateinit var KYM_NO_DESCRIPTION: String
	lateinit var KYM_ORIGIN: String
	lateinit var KYM_DATE: String
	lateinit var KYM_UNKNOWN: String

	// IsUpCommand.kt
	lateinit var ISUP_DESCRIPTION: String
	lateinit var ISUP_ONLINE: String
	lateinit var ISUP_OFFLINE: String
	lateinit var ISUP_UNKNOWN_HOST: String

	// EncurtarCommand.kt
	lateinit var BITLY_INVALID: String

	// TODO: DicioCommand.kt

	// CalculadoraCommand.kt
	lateinit var CALC_DESCRIPTION: String
	lateinit var CALC_RESULT: String
	lateinit var CALC_INVALID: String

	// AnagramaCommand.kt
	lateinit var ANAGRAMA_DESCRIPTION: String
	lateinit var ANAGRAMA_RESULT: String

	// Md5Command.kt
	lateinit var MD5_DESCRIPTION: String
	lateinit var MD5_RESULT: String

	// AminoCommand.kt
	lateinit var AMINO_MEMBERS: String
	lateinit var AMINO_LANGUAGE: String
	lateinit var AMINO_COMMUNITY_HEAT: String
	lateinit var AMINO_CREATED_IN: String
	lateinit var AMINO_COULDNT_FIND: String
	lateinit var AMINO_YOUR_IMAGE: String
	lateinit var AMINO_NO_IMAGE_FOUND: String
	lateinit var AMINO_CONVERT: String

	// MoneyCommand.kt
	lateinit var MONEY_DESCRIPTION: String
	lateinit var MONEY_INVALID_CURRENCY: String
	lateinit var MONEY_CONVERTED: String

	// MorseCommand.kt
	lateinit var MORSE_DESCRIPTION: String
	lateinit var MORSE_FROM_TO: String
	lateinit var MORSE_TO_FROM: String
	lateinit var MORSE_FAIL: String

	// OCRCommand.kt
	lateinit var OCR_COUDLNT_FIND: String

	// PackageInfo.kt
	lateinit var PACKAGEINFO_INVALID: String

	// TempoCommand.kt
	lateinit var TEMPO_DESCRIPTION: String
	lateinit var TEMPO_PREVISAO_PARA: String
	lateinit var TEMPO_TEMPERATURA: String
	lateinit var TEMPO_UMIDADE: String
	lateinit var TEMPO_VELOCIDADE_VENTO: String
	lateinit var TEMPO_PRESSAO_AR: String
	lateinit var TEMPO_ATUAL: String
	lateinit var TEMPO_MAX: String
	lateinit var TEMPO_MIN: String
	lateinit var TEMPO_COULDNT_FIND: String

	// WikipediaCommand.kt
	lateinit var WIKIPEDIA_DESCRIPTION: String
	lateinit var WIKIPEDIA_COULDNT_FIND: String
}