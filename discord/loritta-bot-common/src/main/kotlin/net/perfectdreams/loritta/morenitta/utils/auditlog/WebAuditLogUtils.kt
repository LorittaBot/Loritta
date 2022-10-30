package net.perfectdreams.loritta.morenitta.utils.auditlog

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.AuditLog
import net.perfectdreams.loritta.morenitta.utils.ActionType
import org.jetbrains.exposed.sql.insert

object WebAuditLogUtils {
    fun addEntry(loritta: LorittaBot, guild: Guild, user: User, type: ActionType, params: JsonObject = jsonObject()) =
        addEntry(loritta, guild, user.idLong, type, params)

    fun addEntry(loritta: LorittaBot, guildId: Long, user: User, type: ActionType, params: JsonObject = jsonObject()) =
        addEntry(loritta, guildId, user.idLong, type, params)

    fun addEntry(loritta: LorittaBot, guild: Guild, userId: Long, type: ActionType, params: JsonObject = jsonObject()) =
        addEntry(loritta, guild.idLong, userId, type, params)

    fun addEntry(
        loritta: LorittaBot,
        guildId: Long,
        userId: Long,
        type: ActionType,
        params: JsonObject = jsonObject()
    ) {
        runBlocking {
            loritta.pudding.transaction {
                AuditLog.insert {
                    it[AuditLog.guildId] = guildId
                    it[AuditLog.userId] = userId
                    it[AuditLog.executedAt] = System.currentTimeMillis()
                    it[AuditLog.actionType] = type
                    it[AuditLog.params] = params
                }
            }
        }
    }

    fun fromTargetType(type: String): ActionType {
        return when (type) {
            "default" -> ActionType.UPDATE_GENERAL
            "event_log" -> ActionType.UPDATE_EVENT_LOG
            "invite_blocker" -> ActionType.UPDATE_INVITE_BLOCK
            "autorole" -> ActionType.UPDATE_AUTOROLE
            "permissions" -> ActionType.UPDATE_PERMISSIONS
            "welcomer" -> ActionType.UPDATE_WELCOMER
            "starboard" -> ActionType.UPDATE_STARBOARD
            "music" -> ActionType.UPDATE_MUSIC
            "amino" -> ActionType.UNKNOWN
            "youtube" -> ActionType.UPDATE_YOUTUBE
            "livestream" -> ActionType.UPDATE_TWITCH
            "feeds" -> ActionType.UNKNOWN
            "nashorn_commands" -> ActionType.UPDATE_CUSTOM_COMMANDS
            "event_handlers" -> ActionType.UNKNOWN
            "vanilla_commands" -> ActionType.UPDATE_COMMAND_LIST
            "text_channels" -> ActionType.UPDATE_TEXT_CHANNELS
            "moderation" -> ActionType.UPDATE_MODERATION
            "partner" -> ActionType.UNKNOWN
            "server_list" -> ActionType.UNKNOWN
            "timers" -> ActionType.UPDATE_TIMERS
            "badge" -> ActionType.UPDATE_CUSTOM_BADGE
            "daily_multiplier" -> ActionType.UPDATE_DAILY_MULTIPLIER
            "level" -> ActionType.UPDATE_LEVEL_UP
            "miscellaneous" -> ActionType.UPDATE_MISCELLANEOUS
            "premium" -> ActionType.UPDATE_PREMIUM
            "economy" -> ActionType.UPDATE_ECONOMY
            "reset_xp" -> ActionType.RESET_XP
            "twitter" -> ActionType.UPDATE_TWITTER
            "rss_feeds" -> ActionType.UPDATE_RSS_FEEDS

            else -> ActionType.UNKNOWN
        }
    }
}