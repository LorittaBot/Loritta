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

		// YoutubeMp3Command.kt
		YOUTUBEMP3_ERROR_WHEN_CONVERTING = "An error ocurred when trying to convert the video to MP3... \uD83D\uDE1E"
	}
}