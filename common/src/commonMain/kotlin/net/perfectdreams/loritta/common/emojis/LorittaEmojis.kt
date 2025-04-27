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

    private fun applicationEmoji(emojiName: String): LorittaEmojiReference.ApplicationEmoji {
        val ref = LorittaEmojiReference.ApplicationEmoji(emojiName)
        applicationEmojis.add(ref)
        return ref
    }

    private fun unicodeEmoji(unicodeEmoji: String) = LorittaEmojiReference.UnicodeEmoji(unicodeEmoji)
}