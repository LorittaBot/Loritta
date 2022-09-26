package net.perfectdreams.loritta.legacy.platform.discord

import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.loritta
import net.perfectdreams.loritta.legacy.api.entities.LorittaEmote
import net.perfectdreams.loritta.legacy.api.entities.UnicodeEmote
import net.perfectdreams.loritta.legacy.platform.discord.legacy.entities.DiscordEmote
import net.perfectdreams.loritta.legacy.utils.Emotes
import net.perfectdreams.loritta.legacy.utils.HoconUtils.decodeFromFile
import java.io.File

class DiscordEmoteManager : Emotes.EmoteManager {
    override fun loadEmotes() {
        Emotes.resetEmotes()
        val emoteMap = Constants.HOCON.decodeFromFile<Map<String, String>>(File("${loritta.instanceConfig.loritta.folders.root}emotes.conf"))
        Emotes.emoteMap = emoteMap
    }

    override fun getEmoteByCode(code: String): LorittaEmote {
        return if (code.startsWith("discord:")) {
            DiscordEmote(code)
        } else {
            UnicodeEmote(code)
        }
    }
}
