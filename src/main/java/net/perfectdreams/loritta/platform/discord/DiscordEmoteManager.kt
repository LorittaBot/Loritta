package net.perfectdreams.loritta.platform.discord

import net.perfectdreams.loritta.api.entities.LorittaEmote
import net.perfectdreams.loritta.api.entities.UnicodeEmote
import net.perfectdreams.loritta.platform.discord.entities.DiscordEmote
import net.perfectdreams.loritta.utils.Emotes

class DiscordEmoteManager : Emotes.EmoteManager {
    override fun getEmoteByCode(code: String): LorittaEmote {
        return if (code.startsWith("discord:")) {
            DiscordEmote(code)
        } else {
            UnicodeEmote(code)
        }
    }
}
