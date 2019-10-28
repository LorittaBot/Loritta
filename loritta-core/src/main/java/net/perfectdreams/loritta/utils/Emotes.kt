package net.perfectdreams.loritta.utils

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import net.perfectdreams.loritta.api.entities.LorittaEmote
import net.perfectdreams.loritta.api.entities.UnicodeEmote
import java.io.File
import javax.naming.OperationNotSupportedException

object Emotes {
    private val lazyMgr = resettableManager()
    private val logger = KotlinLogging.logger {}
    var emoteManager: Emotes.EmoteManager? = null

    val MISSING_EMOTE = UnicodeEmote("\uD83D\uDC1B")
    val ONLINE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("online") }
    val IDLE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("idle") }
    val DO_NOT_DISTURB: LorittaEmote by resettableLazy(lazyMgr) { getEmote("do_not_disturb") }
    val OFFLINE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("offline") }
    val BOT_TAG: LorittaEmote by resettableLazy(lazyMgr) { getEmote("bot_tag") }
    val WUMPUS_BASIC: LorittaEmote by resettableLazy(lazyMgr) { getEmote("wumpus_basic") }
    val LORI_TEMMIE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_temmie") }
    val LORI_OWO: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_owo") }
    val LORI_HUG: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_hug") }
    val LORI_HAPPY: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_happy") }
    val LORI_CRYING: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_crying") }
    val LORI_RAGE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_rage") }
    val LORI_SHRUG: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_shrug") }
    val LORI_NITRO_BOOST: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_nitro_boost") }
    val LORI_WOW: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_wow") }
    val LORI_SMILE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_smile") }
    val LORI_HM: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_hm") }
    val LORI_RICH: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_rich") }
    val LORI_PAT: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_pat") }
    val LORI_YAY: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_yay") }
    val LORI_HEART: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_heart") }
    val MINECRAFT_GRASS: LorittaEmote by resettableLazy(lazyMgr) { getEmote("minecraft_grass") }
    val DEFAULT_DANCE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("default_dance") }
    val KOTLIN: LorittaEmote by resettableLazy(lazyMgr) { getEmote("kotlin") }
    val JDA: LorittaEmote by resettableLazy(lazyMgr) { getEmote("jda") }

    private var emoteMap = mapOf<String, String>()

    fun loadEmotes() {
        resetEmotes()
        val emoteMap = Constants.HOCON_MAPPER.readValue<Map<String, String>>(File("${loritta.instanceConfig.loritta.folders.root}emotes.conf"))
        this.emoteMap = emoteMap
    }

    fun getEmote(name: String): LorittaEmote {
        val code = emoteMap[name] ?: run {
            logger.warn("Missing emote for $name")
            return MISSING_EMOTE
        }

        val emoteManager = emoteManager ?: throw OperationNotSupportedException("emoteManager is null!")
        return emoteManager.getEmoteByCode(code)
    }

    fun resetEmotes() {
        lazyMgr.reset()
    }

    interface EmoteManager {
        fun getEmoteByCode(code: String): LorittaEmote

        class DefaultEmoteManager : EmoteManager {
            override fun getEmoteByCode(code: String) = UnicodeEmote(code)
        }
    }
}