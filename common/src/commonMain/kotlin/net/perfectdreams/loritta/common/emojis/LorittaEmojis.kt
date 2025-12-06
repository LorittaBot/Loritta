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
    val LoriAchocolatado = applicationEmoji("lori_achocolatado")
    val LoriSmug = applicationEmoji("lori_smug")
    val LoriExpressionless = applicationEmoji("lori_expressionless")
    val LoriComfy = applicationEmoji("lori_comfy")
    val LoriCoffee = applicationEmoji("lori_coffee")
    val LoriConfetti = applicationEmoji("lori_confetti")

    // Card Clubs
    val CardClubs = applicationEmoji("card_clubs")
    val CardClubsLeft = applicationEmoji("card_clubs_left")
    val CardClubs2Right = applicationEmoji("card_clubs_2_right")
    val CardClubs3Right = applicationEmoji("card_clubs_3_right")
    val CardClubs4Right = applicationEmoji("card_clubs_4_right")
    val CardClubs5Right = applicationEmoji("card_clubs_5_right")
    val CardClubs6Right = applicationEmoji("card_clubs_6_right")
    val CardClubs7Right = applicationEmoji("card_clubs_7_right")
    val CardClubs8Right = applicationEmoji("card_clubs_8_right")
    val CardClubs9Right = applicationEmoji("card_clubs_9_right")
    val CardClubs10Right = applicationEmoji("card_clubs_10_right")
    val CardClubsARight = applicationEmoji("card_clubs_a_right")
    val CardClubsJRight = applicationEmoji("card_clubs_j_right")
    val CardClubsKRight = applicationEmoji("card_clubs_k_right")
    val CardClubsQRight = applicationEmoji("card_clubs_q_right")

    // Card Diamonds
    val CardDiamonds = applicationEmoji("card_diamonds")
    val CardDiamondsLeft = applicationEmoji("card_diamonds_left")
    val CardDiamonds2Right = applicationEmoji("card_diamonds_2_right")
    val CardDiamonds3Right = applicationEmoji("card_diamonds_3_right")
    val CardDiamonds4Right = applicationEmoji("card_diamonds_4_right")
    val CardDiamonds5Right = applicationEmoji("card_diamonds_5_right")
    val CardDiamonds6Right = applicationEmoji("card_diamonds_6_right")
    val CardDiamonds7Right = applicationEmoji("card_diamonds_7_right")
    val CardDiamonds8Right = applicationEmoji("card_diamonds_8_right")
    val CardDiamonds9Right = applicationEmoji("card_diamonds_9_right")
    val CardDiamonds10Right = applicationEmoji("card_diamonds_10_right")
    val CardDiamondsARight = applicationEmoji("card_diamonds_a_right")
    val CardDiamondsJRight = applicationEmoji("card_diamonds_j_right")
    val CardDiamondsKRight = applicationEmoji("card_diamonds_k_right")
    val CardDiamondsQRight = applicationEmoji("card_diamonds_q_right")

    // Card Hearts
    val CardHearts = applicationEmoji("card_hearts")
    val CardHeartsLeft = applicationEmoji("card_hearts_left")
    val CardHearts2Right = applicationEmoji("card_hearts_2_right")
    val CardHearts3Right = applicationEmoji("card_hearts_3_right")
    val CardHearts4Right = applicationEmoji("card_hearts_4_right")
    val CardHearts5Right = applicationEmoji("card_hearts_5_right")
    val CardHearts6Right = applicationEmoji("card_hearts_6_right")
    val CardHearts7Right = applicationEmoji("card_hearts_7_right")
    val CardHearts8Right = applicationEmoji("card_hearts_8_right")
    val CardHearts9Right = applicationEmoji("card_hearts_9_right")
    val CardHearts10Right = applicationEmoji("card_hearts_10_right")
    val CardHeartsARight = applicationEmoji("card_hearts_a_right")
    val CardHeartsJRight = applicationEmoji("card_hearts_j_right")
    val CardHeartsKRight = applicationEmoji("card_hearts_k_right")
    val CardHeartsQRight = applicationEmoji("card_hearts_q_right")

    // Card Spades
    val CardSpades = applicationEmoji("card_spades")
    val CardSpadesLeft = applicationEmoji("card_spades_left")
    val CardSpades2Right = applicationEmoji("card_spades_2_right")
    val CardSpades3Right = applicationEmoji("card_spades_3_right")
    val CardSpades4Right = applicationEmoji("card_spades_4_right")
    val CardSpades5Right = applicationEmoji("card_spades_5_right")
    val CardSpades6Right = applicationEmoji("card_spades_6_right")
    val CardSpades7Right = applicationEmoji("card_spades_7_right")
    val CardSpades8Right = applicationEmoji("card_spades_8_right")
    val CardSpades9Right = applicationEmoji("card_spades_9_right")
    val CardSpades10Right = applicationEmoji("card_spades_10_right")
    val CardSpadesARight = applicationEmoji("card_spades_a_right")
    val CardSpadesJRight = applicationEmoji("card_spades_j_right")
    val CardSpadesKRight = applicationEmoji("card_spades_k_right")
    val CardSpadesQRight = applicationEmoji("card_spades_q_right")

    // Card Unknown
    val CardUnknownLeft = applicationEmoji("card_unknown_left")
    val CardUnknownRight = applicationEmoji("card_unknown_right")

    val Nothing = applicationEmoji("nothing")

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
