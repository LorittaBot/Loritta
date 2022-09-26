package net.perfectdreams.loritta.morenitta.platform.discord

import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.loritta
import net.perfectdreams.loritta.common.entities.LorittaEmote
import net.perfectdreams.loritta.common.entities.UnicodeEmote
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordEmote
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.HoconUtils.decodeFromFile
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
