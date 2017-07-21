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
		MONEY_CONVERTED = "💵 **{0} {1} to {2}**: {3} {1}"

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