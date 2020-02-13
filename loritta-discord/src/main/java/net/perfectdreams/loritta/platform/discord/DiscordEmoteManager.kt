package net.perfectdreams.loritta.platform.discord

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.entities.LorittaEmote
import net.perfectdreams.loritta.api.entities.UnicodeEmote
import net.perfectdreams.loritta.platform.discord.entities.DiscordEmote
import net.perfectdreams.loritta.utils.Emotes
import java.io.File

class DiscordEmoteManager : Emotes.EmoteManager {
    override fun loadEmotes() {
        Emotes.resetEmotes()
        val emoteMap = Constants.HOCON_MAPPER.readValue<Map<String, String>>(File("${loritta.instanceConfig.loritta.folders.root}emotes.conf"))
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
