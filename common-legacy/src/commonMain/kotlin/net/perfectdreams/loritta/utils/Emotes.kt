package net.perfectdreams.loritta.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.api.entities.LorittaEmote
import net.perfectdreams.loritta.api.entities.UnicodeEmote

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
	val LORI_HMPF: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_hmpf") }
	val LORI_DEMON: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_demon") }
	val LORI_BAN_HAMMER: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_ban_hammer") }
	val LORI_COFFEE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_coffee") }
	val LORI_NICE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_nice") }
	val LORI_PRAY: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_pray") }
	val LORI_STONKS: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_stonks") }
	val LORI_HEART1: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_heart_1") }
	val LORI_HEART2: LorittaEmote by resettableLazy(lazyMgr) { getEmote("lori_heart_2") }

	val MINECRAFT_GRASS: LorittaEmote by resettableLazy(lazyMgr) { getEmote("minecraft_grass") }
	val DEFAULT_DANCE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("default_dance") }
	val FLOW_PODCAST: LorittaEmote by resettableLazy(lazyMgr) { getEmote("flow_podcast") }
	val KOTLIN: LorittaEmote by resettableLazy(lazyMgr) { getEmote("kotlin") }
	val JDA: LorittaEmote by resettableLazy(lazyMgr) { getEmote("jda") }
	val ROBLOX_PREMIUM: LorittaEmote by resettableLazy(lazyMgr) { getEmote("roblox_premium") }
	val DISCORD_BOT_LIST: LorittaEmote by resettableLazy(lazyMgr) { getEmote("discord_bot_list") }

	// DISCORD BADGES
	val DISCORD_STAFF: LorittaEmote by resettableLazy(lazyMgr) { getEmote("discord_staff") }
	val DISCORD_PARTNER: LorittaEmote by resettableLazy(lazyMgr) { getEmote("discord_partner") }
	val VERIFIED_DEVELOPER: LorittaEmote by resettableLazy(lazyMgr) { getEmote("verified_developer") }
	val HYPESQUAD_EVENTS: LorittaEmote by resettableLazy(lazyMgr) { getEmote("hypesquad_events") }
	val EARLY_SUPPORTER: LorittaEmote by resettableLazy(lazyMgr) { getEmote("early_supporter") }
	val BUG_HUNTER_1: LorittaEmote by resettableLazy(lazyMgr) { getEmote("bug_hunter_1") }
	val BUG_HUNTER_2: LorittaEmote by resettableLazy(lazyMgr) { getEmote("bug_hunter_2") }

	// HYPESQUAD BADGES
	val BRAVERY_HOUSE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("bravery_house") }
	val BRILLIANCE_HOUSE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("brilliance_house") }
	val BALANCE_HOUSE: LorittaEmote by resettableLazy(lazyMgr) { getEmote("balance_house") }

	var emoteMap = mapOf<String, String>()

	fun getEmote(name: String): LorittaEmote {
		val code = emoteMap[name] ?: run {
			logger.warn { "Missing emote for $name" }
			return MISSING_EMOTE
		}

		val emoteManager = emoteManager ?: throw RuntimeException("emoteManager is null!")
		return emoteManager.getEmoteByCode(code)
	}

	fun resetEmotes() {
		lazyMgr.reset()
	}

	interface EmoteManager {
		fun loadEmotes()
		fun getEmoteByCode(code: String): LorittaEmote

		class DefaultEmoteManager : EmoteManager {
			override fun loadEmotes() {}

			override fun getEmoteByCode(code: String) = UnicodeEmote(code)
		}
	}
}