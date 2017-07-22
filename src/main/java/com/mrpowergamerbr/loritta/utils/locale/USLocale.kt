package com.mrpowergamerbr.loritta.utils.locale

/**
 * US localization
 *
 * @author MrPowerGamerBR
 */
class USLocale : BaseLocale() {
	init {
		// Generic
		SEARCH = "search"
		PROCESSING = "Processing"
		INVALID_NUMBER = "`{0}` is a unrecognizable number for a bot like me, sorry. \uD83D\uDE22"
		MINUTES_AND_SECONDS = "%02d minutes and %02d seconds"

		// Event Log
		EVENTLOG_USER_ID = "User ID: {0}"
		EVENTLOG_AVATAR_CHANGED = "**{0} changed avatars**"
		EVENTLOG_NAME_CHANGED = "**{0} changed username!**\n\nPrevious username: `{1}`\nNew username: `{2}`"
		EVENTLOG_CHANNEL_CREATED = "**Created text channel {0}**"
		EVENTLOG_CHANNEL_NAME_UPDATED = "**{0}''s name was changed!**\n\nPrevious name: `{1}`\nNew name: `{2}`"
		EVENTLOG_CHANNEL_POSITION_UPDATED = "**{0}''s position was changed!**\n\nPrevious position: `{1}`\nNew position: `{2}`"
		EVENTLOG_CHANNEL_DELETED = "**Deleted text channel `{0}`**"

		// CommandBase.kt
		HOW_TO_USE = "How to use"
		EXAMPLE = "Example"

		// BackgroundCommand.kt
		BACKGROUND_DESCRIPTION = "Why not spice up your profile with a new fancy wallpaper?"
		BACKGROUND_CENTRAL = "Wallpaper Central"
		BACKGROUND_INFO = "**Do you want to change your profile''s wallpaper? Then you came to the right place!**\n" +
				"\n" +
				"Click in \uD83D\uDDBC to view your current wallpaper\n" +
				"Click in \uD83D\uDED2 to view the default templates" +
				"\n" +
				"\n" +
				"Want to send your own wallpaper? No problem! Send a 400x300 image in chat and, with the image, write `{0}background`! (You can also send the image link with the command that I will also accept it!)\n\n(Don''t send NSFW wallpapers! If you send and get caught, your account will be suspended and you won''t be able to use me!)"
		BACKGROUND_INVALID_IMAGE = "Invalid image! Are you sure this is a valid link? If you can, please download the image and upload it directly on Discord!"
		BACKGROUND_UPDATED = "Wallpaper updated!"
		BACKGROUND_EDITED = "Because your image wasn''t 400x300, I needed to change it a little bit!"
		BACKGROUND_YOUR_CURRENT_BG = "Your current wallpaper"
		BACKGROUND_TEMPLATE_INFO = "Click in ⬅ to go back one template\n" +
				"Click in ➡ to view the next template\n" +
				"Click in ✅ to use this template as your wallpaper"

		// DiscriminatorCommand.kt
		DISCRIM_DESCRIPTION = "View all users who share a common discriminator with you or with another user!"
		DISCRIM_NOBODY = "Nobody who I know has the discriminator `#${0}`!"

		// RankCommand.kt
		RANK_DESCRIPTION = "View this server rank!"
		RANK_INFO = "Total XP: {0} | Current Level: {1}"

		// RepCommand.kt
		REP_DESCRIPTON = "Give reputation to another user!"
		REP_SELF = "You can't give reputation to yourself, silly!"
		REP_WAIT = "You need to wait **{0}** before giving another reputation!"
		REP_SUCCESS = "gave a reputation point to {0}!"

		// SobreMimCommand.kt
		SOBREMIM_DESCRIPTION = "Set your personal \"about me\" message in your profile!"
		SOBREMIM_CHANGED = "Your profile message was changed to `{0}`!"

		// HelloWorldCommand.kt
		HELLO_WORLD = "Hello world! {0}"
		HELLO_WORLD_DESCRIPTION = "A simple command used to test Loritta's locale system."
		USING_LOCALE = "Using {0} as locale!"

		// ===[ COMMANDS - MUSIC ]===
		// MusicInfoCommand.kt & PlaylistCommand.kt
		MUSICINFO_DESCRIPTION = "Shows the current track."
		MUSICINFO_NOMUSIC = "Nothing is playing right now... Why don''t you play one? `+play music`"
		MUSICINFO_INQUEUE = "In queue..."
		MUSICINFO_NOMUSIC_SHORT = "No tracks..."
		MUSICINFO_REQUESTED_BY = "requested by"
		MUSICINFO_LENGTH = "Length"
		MUSICINFO_VIEWS = "Views"
		MUSICINFO_LIKES = "Likes"
		MUSICINFO_DISLIKES = "Dislikes"
		MUSICINFO_COMMENTS = "Comments"
		MUSICINFO_SKIPTITLE = "Do you want to skip this track?"
		MUSICINFO_SKIPTUTORIAL = "**Then react with \uD83E\uDD26 in this message!** (If 75% of the users in the music channel reacts with \uD83E\uDD26, I will skip the song!)"

		// PularCommand.kt
		PULAR_DESCRIPTION = "Skips a track."
		PULAR_MUSICSKIPPED = "Track skipped!"

		// TocarCommand.kt
		TOCAR_DESCRIPTION = "Queues a track to be played!"
		TOCAR_MUTED = "Someone muted me in the voice channel... \uD83D\uDE1E Please, ask someone of this server administration to unmute me!"
		TOCAR_CANTTALK = "I don't have permission to talk in the voice channel... \uD83D\uDE1E Please, ask someone of this server administration to give permission so I can play some beats!"
		TOCAR_NOTINCHANNEL = "You need to be in the music channel to queue songs!"

		// VolumeCommand.kt
		VOLUME_DESCRIPTION = "Change the song volume"
		VOLUME_TOOHIGH = "Do you wanna be deaf? Well, you can, but I''m also listening and I don''t want to."
		VOLUME_TOOLOW = "Nope, using negative numbers won''t mute it so much that would cause it to be permanently banned from earth."
		VOLUME_LOWER = "I will turn down the song volume! Sorry if I bothered you with the volume..."
		VOLUME_HIGHER = "I will turn up the song volume! You better not play trash!"
		VOLUME_EXCEPTION = "Okay, let''s change the volume to 💩 then... I wonder how I will change the volume to that..."

		// ~ generic ~
		MUSIC_MAX = "Track too long! A track must have, at least `{0}`!"
		MUSIC_ADDED = "Added to the queue `{0}`!"
		MUSIC_PLAYLIST_ADDED = "Added to queue {0} songs!"
		MUSIC_PLAYLIST_ADDED_IGNORED = "Added to the queue {0} songs! (ignored {1} tracks due to its length!)"
		MUSIC_NOTFOUND = "I couldn''t find anything related to `{0}` on YouTube... Try using the video link instead of searching for it!"
		MUSIC_ERROR = "Ih Serjão Sujou!\n`{0}`\n(Probably it is a VEVO video and they only let you watch the video in YouTube''s website... \uD83D\uDE22)"

		// ===[ COMMANDS - UNDERTALE ]===
		// UndertaleBattleCommand.kt
		UTBATTLE_DESCRIPTION = "Creates a speech bubble like in Undertale"
		UTBATTLE_INVALID = "Monster `{0}` is not valid! **Valid monsters:** `{1}`"

		// UndertaleBoxCommand.kt
		UTBOX_DESCRIPTION = "Creates a dialog box like in Undertale"

		// LembrarCommand.kt
		LEMBRAR_DESCRIPTION = "Do you need to remember to give food to your doggo? Maybe you want to create a reminder so in the future you can see if you could do all your \"Life Goals\" of this year? Then create a reminder!"
		LEMBRAR_SUCCESS = "I will remind you in {0}/{1}/{2} {3}:{4}!"

		// KnowYourMemeCommand.kt
		KYM_DESCRIPTION = "Search a meme in KnowYourMeme"
		KYM_COULDNT_FIND = "I couldn''t find anything related to `{0}`!"
		KYM_NO_DESCRIPTION = "No description..."
		KYM_ORIGIN = "Origin"
		KYM_DATE = "Date"
		KYM_UNKNOWN = "Unknown"

		// IsUpCommand.kt
		ISUP_DESCRIPTION = "Verify if a website is online!"
		ISUP_ONLINE = "It''s only you, for me `{0}` is online! (**Response:** {1})"
		ISUP_OFFLINE = "It''s not only you, for me `{0}` is also offline! (**Error:** {1})"
		ISUP_UNKNOWN_HOST = "`{0} doesn''t exist!`"

		// HexCommand.kt
		HEX_DESCRIPTION = "Transform a RGB color to hexadecimal"
		HEX_RESULT = "I transformed your color `{0}, {1}, {2} {3}` to hexadecimal! `{4}`"
		HEX_BAD_ARGS = "All arguments must be numbers!"

		// EncurtarCommand.kt
		BITLY_DESCRIPTION = "Shorten a link using bit.ly"
		BITLY_INVALID = "`{0}` is an invalid URL!"

		// TODO: DicioCommand.kt

		// CalculadoraCommand.kt
		CALC_DESCRIPTION = "Calculates a arithmetic expression"
		CALC_RESULT = "Result: `{0}`"
		CALC_INVALID = "`{0}` is not a valid artihmetic expression!"

		// BIRLCommand.kt
		BIRL_DESCRIPTION = "Compiles a code created in BIRL (Bambam's \"It's show time\" Recursive Language)"
		BIRL_RESULT = "Result"
		BIRL_INFO = "Codes in BIRL must be within code blocks, example:\n`{0}`For more information: https://birl-language.github.io/"

		// AnagramaCommand.kt
		ANAGRAMA_DESCRIPTION = "Creates a anagram of an word!"
		ANAGRAMA_RESULT = "Your anagram is... `{0}`"

		// Md5Command.kt
		MD5_DESCRIPTION = "Encrypts a message using MD5"
		MD5_RESULT = "`{0}` in MD5: `{1}`"

		// AminoCommand.kt
		AMINO_DESCRIPTION = "Commands related to Amino! ([http://aminoapps.com/](http://aminoapps.com/))"
		AMINO_MEMBERS = "Members"
		AMINO_LANGUAGE = "Language"
		AMINO_COMMUNITY_HEAT = "Community Heat"
		AMINO_CREATED_IN = "Created in"
		AMINO_COULDNT_FIND = "I couldn't find a community related to `{0}`!"
		AMINO_YOUR_IMAGE = "Your image `{0}`!"
		AMINO_NO_IMAGE_FOUND = "I didn't find any \".Amino\" images in your message... \uD83D\uDE1E"
		AMINO_CONVERT = "convert"

		// MoneyCommand.kt
		MONEY_DESCRIPTION = "Converts a currency to another. (Example: View how much dollar is costing compared to real)"
		MONEY_INVALID_CURRENCY = "`{0}` is not a valid currency! \uD83D\uDCB8\n**Valid currencies:** {1}"
		MONEY_CONVERTED = "💵 **{0} {1} to {2}**: {3} {2}"

		// MorseCommand.kt
		MORSE_DESCRIPTION = "Encodes/Decodes a message in morse code"
		MORSE_FROM_TO = "Text to Morse"
		MORSE_TO_FROM = "Morse to Text"
		MORSE_FAIL = "I couldn''t transform your message in morse code... Maybe you used characters that doesn''t exist in morse code!"

		// OCRCommand.kt
		OCR_DESCRIPTION = "Reads the text in a image using OCR"
		OCR_COUDLNT_FIND = "I didn''t find any text in that image..."

		// PackageInfo.kt
		PACKAGEINFO_DESCRIPTION = "Shows the status of a postal package, works with Correios (Brasil) and CTT (Portugal)"
		PACKAGEINFO_INVALID = "`{0}` is not a valid postal code!"
		PACKAGEINFO_COULDNT_FIND = "I didn''t find the object `{0}` in Correios'' database!"

		// RgbCommand.kt
		RGB_DESCRIPTION = "Transform a hexadecimal color to RGB"
		RGB_TRANSFORMED = "I transformed your color `{0}` to RGB! {1}, {2}, {3}"
		RGB_INVALID = "The color `{0}` is not a valid hexadecimal color!"

		// TempoCommand.kt
		TEMPO_DESCRIPTION = "Verify the temperature of a city!"
		TEMPO_PREVISAO_PARA = "Weather forecast for {0}, {1}"
		TEMPO_TEMPERATURA = "Temperature"
		TEMPO_UMIDADE = "Humidity"
		TEMPO_VELOCIDADE_VENTO = "Wind Speed"
		TEMPO_PRESSAO_AR = "Air Pressure"
		TEMPO_ATUAL = "Current"
		TEMPO_MAX = "Maximum"
		TEMPO_MIN = "Minium"
		TEMPO_COULDNT_FIND = "I didn''t find any city named `{0}`!"

		// TranslateCommand.kt
		TRANSLATE_DESCRIPTION = "Translates a sentence to another language"

		// WikipediaCommand.kt
		WIKIPEDIA_DESCRIPTION = "Shows a summary of a Wikipedia page"
		WIKIPEDIA_COULDNT_FIND = "I didn''t find anything related to `{0}`!"

		// YoutubeMp3Command.kt
		YOUTUBEMP3_DESCRIPTION = "Download a YouTube video in MP3!"
		YOUTUBEMP3_ERROR_WHEN_CONVERTING = "An error ocurred when trying to convert the video to MP3... \uD83D\uDE1E"
		YOUTUBEMP3_INVALID_LINK = "Invalid link!"
		YOUTUBEMP3_DOWNLOADING_VIDEO = "Downloading video"
		YOUTUBEMP3_CONVERTING_VIDEO = "Converting video"
		YOUTUBEMP3_FINISHED = "Done! Your video is now ready to be downloaded in MP3! {0}"
	}
}