package net.perfectdreams.loritta.cinnamon.platform.interaktions.emotes

import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.common.emotes.EmoteManager
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.emotes.UnicodeEmote

class DiscordEmoteManager(
    val emoteMap: Map<String, String>
) : EmoteManager {
    override fun getEmoteByName(name: String): Emote {
        return getEmoteByCode(emoteMap[name] ?: return Emotes.missingEmote)
    }

    override fun getEmoteByCode(code: String): Emote {
        return if (code.startsWith("discord:")) {
            DiscordEmote(code)
        } else {
            UnicodeEmote(code)
        }
    }
}
