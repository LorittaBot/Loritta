package net.perfectdreams.loritta.common.loricoolcards

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.utils.Color

enum class CardRarity(
    val color: Color,
    val emoji: DiscordEmote
) {
    COMMON(Color(87, 87, 87), Emotes.StickerRarityCommon),
    UNCOMMON(Color(75, 160, 50), Emotes.StickerRarityUncommon),
    RARE(Color(32, 138, 225), Emotes.LoriCoolSticker),
    EPIC(Color(107, 0, 238), Emotes.StickerRarityEpic),
    LEGENDARY(Color(255, 134, 25), Emotes.StickerRarityLegendary),
    MYTHIC(Color(191, 0, 0), Emotes.StickerRaritySpecial)
}