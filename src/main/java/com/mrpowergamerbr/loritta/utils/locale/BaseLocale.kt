package com.mrpowergamerbr.loritta.utils.locale

/**
 * Classe de localização base, por padrão em PT-BR
 *
 * Locales diferentes devem extender esta classe
 */
open class BaseLocale {
	// Generic
	lateinit var SEARCH: String
	lateinit var PROCESSING: String
	lateinit var INVALID_NUMBER: String
	lateinit var MINUTES_AND_SECONDS: String
	lateinit var NSFW_IMAGE: String
	lateinit var MENTION_RESPONSE: String

	// Event Log
	lateinit var EVENTLOG_USER_ID: String
	lateinit var EVENTLOG_AVATAR_CHANGED: String
	lateinit var EVENTLOG_NAME_CHANGED: String
	lateinit var EVENTLOG_CHANNEL_CREATED: String
	lateinit var EVENTLOG_CHANNEL_NAME_UPDATED: String
	lateinit var EVENTLOG_CHANNEL_TOPIC_UPDATED: String
	lateinit var EVENTLOG_CHANNEL_POSITION_UPDATED: String
	lateinit var EVENTLOG_CHANNEL_DELETED: String

	// CommandBase.kt
	lateinit var HOW_TO_USE: String
	lateinit var EXAMPLE: String

	// ===[ WEBSITE ]===
	lateinit var WEBSITE_HELLO: String
	lateinit var WEBSITE_JUST_A_SIMPLE_DISCORD_BOT: String
	lateinit var WEBSITE_ADD_ME: String
	lateinit var WEBSITE_HOME: String
	lateinit var WEBSITE_OUR_DISCORD: String
	lateinit var WEBSITE_ADMIN_PANEL: String
	lateinit var WEBSITE_COMMANDS: String
	lateinit var WEBSITE_FAN_ARTS: String
	lateinit var WEBSITE_DONATE: String
	lateinit var WEBSITE_ABOUT: String
	lateinit var WEBSITE_PARTNERS: String
	lateinit var WEBSITE_MYSERVER: String
	lateinit var WEBSITE_SERVERFANCLUB: String
	lateinit var WEBSITE_STATISTICS: String
	lateinit var WEBSITE_INTRO: String

	// ===[ COMMANDS - ADMINISTRATION ]===
	// HackBanCommand.kt
	lateinit var HACKBAN_DESCRIPTION: String
	lateinit var HACKBAN_BY: String
	lateinit var HACKBAN_REASON: String
	lateinit var HACKBAN_SUCCESS: String
	lateinit var HACKBAN_NO_PERM: String

	// LimparCommand.kt
	lateinit var LIMPAR_DESCRIPTION: String
	lateinit var LIMPAR_INVALID_RANGE: String
	lateinit var LIMPAR_SUCCESS: String

	// MuteCommand.kt
	lateinit var MUTE_DESCRIPTION: String
	lateinit var MUTE_CANT_MUTE_ME: String
	lateinit var MUTE_ROLE_NAME: String
	lateinit var MUTE_NO_PERM: String
	lateinit var MUTE_SUCCESS_ON: String
	lateinit var MUTE_SUCCESS_OFF: String

	// RoleIdCommand.kt
	lateinit var ROLEID_DESCRIPTION: String

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
	lateinit var AVATAR_CLICKHERE: String
	lateinit var AVATAR_LORITTACUTE: String

	// BotInfoCommand.kt
	lateinit var BOTINFO_DESCRIPTION: String
	lateinit var BOTINFO_TITLE: String
	lateinit var BOTINFO_EMBED_INFO: String
	lateinit var BOTINFO_HONORABLE_MENTIONS: String
	lateinit var BOTINFO_MENTIONS: String
	lateinit var BOTINFO_CREATEDBY: String

	// EmojiCommand.kt
	lateinit var EMOJI_DESCRIPTION: String

	// InviteCommand.kt
	lateinit var INVITE_DESCRIPTION: String
	lateinit var INVITE_INFO: String

	// ServerInfoCommand.kt
	lateinit var SERVERINFO_DESCRIPTION: String
	lateinit var SERVERINFO_OWNER: String
	lateinit var SERVERINFO_REGION: String
	lateinit var SERVERINFO_CHANNELS: String
	lateinit var SERVERINFO_CHANNELS_TEXT: String
	lateinit var SERVERINFO_CHANNELS_VOICE: String
	lateinit var SERVERINFO_CREATED_IN: String
	lateinit var SERVERINFO_JOINED_IN: String
	lateinit var SERVERINFO_MEMBERS: String
	lateinit var SERVERINFO_ONLINE: String
	lateinit var SERVERINFO_AWAY: String
	lateinit var SERVERINFO_BUSY: String
	lateinit var SERVERINFO_OFFLINE: String
	lateinit var SERVERINFO_PEOPLE: String
	lateinit var SERVERINFO_BOTS: String
	lateinit var SERVERINFO_ROLES: String
	lateinit var SERVERINFO_CUSTOM_EMOJIS: String

	// ===[ COMMANDS - FUN ]===
	// AmigosCommand.kt
	lateinit var AMIGOS_DESCRIPTION: String

	// AmizadeCommand.kt
	lateinit var AMIZADE_DESCRIPTION: String
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
	lateinit var CARACOROA_DESCRIPTION: String
	lateinit var CARACOROA_HEADS: String
	lateinit var CARACOROA_TAILS: String

	// CepoCommand.kt
	lateinit var CEPO_DESCRIPTION: String

	// ClapifyCommand.kt
	lateinit var CLAPIFY_DESCRIPTION: String

	// DeusCommand.kt
	lateinit var DEUS_DESCRIPTION: String

	// DeusesCommand.kt
	lateinit var DEUSES_DESCRIPTION: String

	// DiscordiaCommand.kt
	lateinit var DISCORDIA_DESCRIPTION: String

	// DrakeCommand.kt
	lateinit var DRAKE_DESCRIPTION: String

	// FaustãoCommand.kt
	lateinit var FAUSTAO_DESCRIPTION: String

	// FraseToscaCommand.kt
	lateinit var FRASETOSCA_DESCRIPTION: String
	lateinit var FRASETOSCA_GABRIELA: String

	// GangueCommand.kt
	lateinit var GANGUE_DESCRIPTION: String

	// InverterCommand.kt
	lateinit var INVERTER_DESCRIPTION: String

	// LavaCommand.kt
	lateinit var LAVA_DESCRIPTION: String

	// Lalateinit valeversoCommand.kt
	lateinit var LAVAREVERSO_DESCRIPTION: String

	// TODO: Textos na imagem

	// MagicBallCommand.kt
	lateinit var VIEIRINHA_DESCRIPTION: String
	lateinit var VIEIRINHA_responses: List<String>

	// NyanCatCommand.kt
	lateinit var NYANCAT_DESCRIPTION: String

	// PedraPapelTesouraCommand.kt
	lateinit var PPT_DESCRIPTION: String
	lateinit var PPT_WIN: String
	lateinit var PPT_LOSE: String
	lateinit var PPT_DRAW: String
	lateinit var PPT_CHOSEN: String
	lateinit var PPT_JESUS_CHRIST: String
	lateinit var PPT_MAYBE_DRAW: String
	lateinit var PPT_INVALID: String

	// PerdaoCommand.kt
	lateinit var PERDAO_DESCRIPTION: String

	// PerfeitoCommand.kt
	lateinit var PERFEITO_DESCRIPTION: String

	// PretoEBrancoCommand.kt
	lateinit var PRETOEBRANCO_DESCRIPTION: String

	// PrimeirasPalavrasCommand.kt
	lateinit var PRIMEIRAS_DESCRIPTION: String

	// QuadroCommand.kt
	lateinit var QUADRO_DESCRIPTION: String

	// QualidadeCommand.kt
	lateinit var QUALIDADE_DESCRIPTION: String

	// TODO: RandomSAMCommand.kt & RandomMemeguy1997.kt

	// RazoesCommand.kt
	lateinit var RAZOES_DESCRIPTION: String

	// ReceitasCommand.kt
	lateinit var RECEITAS_DESCRIPTION: String
	lateinit var RECEITAS_INFO: String
	lateinit var RECEITAS_COULDNT_FIND: String

	// RollCommand.kt
	lateinit var ROLL_DESCRIPTION: String
	lateinit var ROLL_INVALID_NUMBER: String
	lateinit var ROLL_RESULT: String

	// SAMCommand.kt
	lateinit var SAM_DESCRIPTION: String

	// ShipCommand.kt
	lateinit var SHIP_DESCRIPTION: String
	lateinit var SHIP_NEW_COUPLE: String

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

	// SpinnerCommand.kt
	lateinit var SPINNER_DESCRIPTION: String
	lateinit var SPINNER_SPINNING: String
	lateinit var SPINNER_SPINNED: String

	// TretaNewsCommand.kt
	lateinit var TRETANEWS_DESCRIPTION: String

	// TristeRealidadeCommand.kt
	lateinit var TRISTEREALIDADE_DESCRIPTION: String
	lateinit var TRISTEREALIDADE_FILE: String

	// TrumpCommand.kt
	lateinit var TRUMP_DESCRIPTION: String

	// VaporondaCommand.kt
	lateinit var VAPORONDA_DESCRIPTION: String

	// VaporQualidadeCommand.kt
	lateinit var VAPORQUALIDADE_DESCRIPTION: String

	// WikiaCommand.kt
	lateinit var WIKIA_DESCRIPTION: String
	lateinit var WIKIA_COULDNT_FIND: String

	// YouTubeCommand.kt
	lateinit var YOUTUBE_DESCRIPTION: String
	lateinit var YOUTUBE_RESULTS_FOR: String
	lateinit var YOUTUBE_COULDNT_FIND: String
	lateinit var YOUTUBE_CHANNEL: String

	// ===[ COMMANDS - MINECRAFT ]===
	// McAvatarCommand.kt
	lateinit var MCAVATAR_DESCRIPTION: String
	lateinit var MCAVATAR_AVATAR_DE: String

	// McBodyCommand.kt
	lateinit var MCBODY_DESCRIPTION: String
	lateinit var MCBODY_BODY_DE: String

	// McHeadCommand.kt
	lateinit var MCHEAD_DESCRIPTION: String
	lateinit var MCHEAD_HEAD_DE: String

	// McQueryCommand.kt
	lateinit var MCQUERY_DESCRIPTION: String
	lateinit var MCQUERY_OFFLINE: String
	lateinit var MCQUERY_VERSION: String
	lateinit var MCQUERY_PROTOCOL: String

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
	lateinit var AJUDA_DESCRIPTION: String
	lateinit var AJUDA_SENT_IN_PRIVATE: String
	lateinit var AJUDA_INTRODUCE_MYSELF: String
	lateinit var AJUDA_MY_HELP: String

	// AngelCommand.kt
	lateinit var ANGEL_DESCRIPTION: String

	// EscolherCommand.kt
	lateinit var ESCOLHER_DESCRIPTION: String
	lateinit var ESCOLHER_RESULT: String

	// PingCommand.kt
	lateinit var PING_DESCRIPTION: String

	// LanguageCommand.kt
	lateinit var LANGUAGE_DESCRIPTION: String
	lateinit var LANGUAGE_INFO: String
	lateinit var LANGUAGE_USING_LOCALE: String

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

	// RankCommand.kt
	lateinit var RANK_DESCRIPTION: String
	lateinit var RANK_INFO: String
	lateinit var RANK_SERVER_RANK: String

	// RepCommand.kt
	lateinit var REP_DESCRIPTON: String
	lateinit var REP_SELF: String
	lateinit var REP_WAIT: String
	lateinit var REP_SUCCESS: String

	// SobreMimCommand.kt
	lateinit var SOBREMIM_DESCRIPTION: String
	lateinit var SOBREMIM_CHANGED: String

	// HelloWorldCommand.kt
	lateinit var HELLO_WORLD: String
	lateinit var HELLO_WORLD_DESCRIPTION: String
	lateinit var USING_LOCALE: String

	// ===[ COMMANDS - MUSIC ]===
	// MusicInfoCommand.kt & PlaylistCommand.kt
	lateinit var MUSICINFO_DESCRIPTION: String
	lateinit var MUSICINFO_NOMUSIC: String
	lateinit var MUSICINFO_INQUEUE: String
	lateinit var MUSICINFO_NOMUSIC_SHORT: String
	lateinit var MUSICINFO_REQUESTED_BY: String
	lateinit var MUSICINFO_LENGTH: String
	lateinit var MUSICINFO_VIEWS: String
	lateinit var MUSICINFO_LIKES: String
	lateinit var MUSICINFO_DISLIKES: String
	lateinit var MUSICINFO_COMMENTS: String
	lateinit var MUSICINFO_SKIPTITLE: String
	lateinit var MUSICINFO_SKIPTUTORIAL: String

	// PularCommand.kt
	lateinit var PULAR_DESCRIPTION: String
	lateinit var PULAR_MUSICSKIPPED: String

	// TocarCommand.kt
	lateinit var TOCAR_DESCRIPTION: String
	lateinit var TOCAR_MUTED: String
	lateinit var TOCAR_CANTTALK: String
	lateinit var TOCAR_NOTINCHANNEL: String

	// VolumeCommand.kt
	lateinit var VOLUME_DESCRIPTION: String
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
	lateinit var MUSIC_NOTFOUND: String
	lateinit var MUSIC_ERROR: String

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
	lateinit var UTBATTLE_DESCRIPTION: String
	lateinit var UTBATTLE_INVALID: String

	// UndertaleBoxCommand.kt
	lateinit var UTBOX_DESCRIPTION: String

	// ===[ COMMANDS - UTILS ]===
	// LembrarCommand.kt
	lateinit var LEMBRAR_DESCRIPTION: String
	lateinit var LEMBRAR_SUCCESS: String

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

	// HexCommand.kt
	lateinit var HEX_DESCRIPTION: String
	lateinit var HEX_RESULT: String
	lateinit var HEX_BAD_ARGS: String

	// EncurtarCommand.kt
	lateinit var BITLY_DESCRIPTION: String
	lateinit var BITLY_INVALID: String

	// TODO: DicioCommand.kt

	// CalculadoraCommand.kt
	lateinit var CALC_DESCRIPTION: String
	lateinit var CALC_RESULT: String
	lateinit var CALC_INVALID: String

	// BIRLCommand.kt
	lateinit var BIRL_DESCRIPTION: String
	lateinit var BIRL_RESULT: String
	lateinit var BIRL_INFO: String

	// AnagramaCommand.kt
	lateinit var ANAGRAMA_DESCRIPTION: String
	lateinit var ANAGRAMA_RESULT: String

	// Md5Command.kt
	lateinit var MD5_DESCRIPTION: String
	lateinit var MD5_RESULT: String

	// AminoCommand.kt
	lateinit var AMINO_DESCRIPTION: String
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
	lateinit var OCR_DESCRIPTION: String
	lateinit var OCR_COUDLNT_FIND: String

	// PackageInfo.kt
	lateinit var PACKAGEINFO_DESCRIPTION: String
	lateinit var PACKAGEINFO_INVALID: String
	lateinit var PACKAGEINFO_COULDNT_FIND: String

	// RgbCommand.kt
	lateinit var RGB_DESCRIPTION: String
	lateinit var RGB_TRANSFORMED: String
	lateinit var RGB_INVALID: String

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

	// TranslateCommand.kt
	lateinit var TRANSLATE_DESCRIPTION: String

	// WikipediaCommand.kt
	lateinit var WIKIPEDIA_DESCRIPTION: String
	lateinit var WIKIPEDIA_COULDNT_FIND: String

	// YoutubeMp3Command.kt
	lateinit var YOUTUBEMP3_DESCRIPTION: String
	lateinit var YOUTUBEMP3_ERROR_WHEN_CONVERTING: String
	lateinit var YOUTUBEMP3_INVALID_LINK: String
	lateinit var YOUTUBEMP3_DOWNLOADING_VIDEO: String
	lateinit var YOUTUBEMP3_CONVERTING_VIDEO: String
	lateinit var YOUTUBEMP3_FINISHED: String
}