package net.perfectdreams.loritta.common.emojis

/**
 * A list of the emojis Loritta uses
 *
 * This only stores an "emoji reference", that is, application "Discord" emojis do not store enough data to create a chat mention from it,
 * it only stores a name to map them to uploaded application emojis.
 */
object LorittaEmojis {
    val applicationEmojis = mutableListOf<LorittaEmojiReference.ApplicationEmoji>()

    val GarticBot = applicationEmoji("garticbot")
    val LoriHead = applicationEmoji("lori_head")
    val PantufaHead = applicationEmoji("pantufa_head")
    val GabrielaHead = applicationEmoji("gabi_head")
    val GessyHead = applicationEmoji("gessy_head")
    val CirnoFumo = applicationEmoji("cirno_fumo")
    val PomniPlush = applicationEmoji("pomni_plush")
    val RalseiPlush = applicationEmoji("ralsei_plush")
    val TailsPlush = applicationEmoji("tails_plush")
    val Role = applicationEmoji("role")

    // Badge Emojis
    val Artist = applicationEmoji("artist", "/badges/")
    val Brat = applicationEmoji("brat", "/badges/")
    val Christmas2019 = applicationEmoji("christmas2019", "/badges/")
    val Christmas2022 = applicationEmoji("christmas2022", "/badges/")
    val DiscordNitro = applicationEmoji("discord_nitro", "/badges/")
    val DiscordBraveryHouse = applicationEmoji("discord_bravery", "/badges/")
    val DiscordBrillianceHouse = applicationEmoji("discord_brilliance", "/badges/")
    val DiscordBalanceHouse = applicationEmoji("discord_balance", "/badges/")
    val DiscordEarlySupporter = applicationEmoji("discord_early_supporter", "/badges/")
    val DiscordPartner = applicationEmoji("discord_partner", "/badges/")
    val DiscordHypesquadEvents = applicationEmoji("hypesquad_events", "/badges/")
    val DiscordVerifiedDeveloper = applicationEmoji("verified_developer", "/badges/")
    val DiscordActiveDeveloper = applicationEmoji("active_developer", "/badges/")
    val DiscordModeratorProgramAlumni = applicationEmoji("moderator_program_alumni", "/badges/")
    val DiscordStaff = applicationEmoji("discord_staff", "/badges/")
    val Easter2023 = applicationEmoji("easter2023", "/badges/")
    val GrassCutter = applicationEmoji("grass_cutter", "/badges/")
    val Halloween2019 = applicationEmoji("halloween2019", "/badges/")
    val LorittaStaff = applicationEmoji("loritta_staff", "/badges/")
    val Married = applicationEmoji("married", "/badges/")
    val LoriCaneca = applicationEmoji("lori_caneca", "/badges/")
    val LoriRich = applicationEmoji("donator", "/badges/")
    val Halloween2024ReactionEventSuper = applicationEmoji("halloween2024_super", "/badges/")
    val Christmas2024ReactionEvent = applicationEmoji("christmas2024", "/badges/")
    val Christmas2024ReactionEventSuper = applicationEmoji("christmas2024_super", "/badges/")
    val Anniversary2025ReactionEvent = applicationEmoji("anniversary2025", "/badges/")
    val Anniversary2025ReactionEventSuper = applicationEmoji("anniversary2025_super", "/badges/")
    val SparklyMember = applicationEmoji("sparkly_member", "/badges/")
    val SparklyStaff = applicationEmoji("sparkly_staff", "/badges/")
    val StickerFan = applicationEmoji("sticker_fan", "/badges/")
    val Stonks = applicationEmoji("stonks", "/badges/")
    val TopLoveLetter = applicationEmoji("top_love_letter", "/badges/")
    val LoriCard = applicationEmoji("super_donator", "/badges/")

    private fun applicationEmoji(emojiName: String, imageFolder: String = "/application_emojis/"): LorittaEmojiReference.ApplicationEmoji {
        val ref = LorittaEmojiReference.ApplicationEmoji(emojiName, "$imageFolder$emojiName")
        applicationEmojis.add(ref)
        return ref
    }

    private fun unicodeEmoji(unicodeEmoji: String) = LorittaEmojiReference.UnicodeEmoji(unicodeEmoji)
}
