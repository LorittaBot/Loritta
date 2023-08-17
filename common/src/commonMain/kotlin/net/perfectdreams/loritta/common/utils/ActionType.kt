package net.perfectdreams.loritta.common.utils

import kotlinx.serialization.Serializable

@Serializable
enum class ActionType(val updateType: String, val sectionName: String) {
    UNKNOWN("generic", "unknown"),

    UPDATE_GENERAL("updated", "general"),
    UPDATE_MODERATION("updated", "moderation"),
    UPDATE_COMMAND_LIST("updated", "commands"),
    UPDATE_PERMISSIONS("updated", "permissions"),
    UPDATE_WELCOMER("updated", "welcomer"),
    UPDATE_EVENT_LOG("updated", "eventLog"),
    UPDATE_YOUTUBE("updated", "unknown"),
    UPDATE_TWITCH("updated", "unknown"),
    UPDATE_AUTOROLE("updated", "autorole"),
    UPDATE_INVITE_BLOCK("updated", "inviteBlocker"),
    UPDATE_MUSIC("updated", "music"),
    UPDATE_ECONOMY("updated", "economy"),
    UPDATE_TIMERS("updated", "timers"),
    UPDATE_STARBOARD("updated", "starboard"),
    UPDATE_MISCELLANEOUS("updated", "miscellaneous"),
    UPDATE_CUSTOM_BADGE("updated", "customBadge"),
    UPDATE_DAILY_MULTIPLIER("updated", "dailyMultiplier"),
    UPDATE_CUSTOM_COMMANDS("updated", "customCommands"),
    UPDATE_LEVEL_UP("updated", "levelUp"),
    UPDATE_TEXT_CHANNELS("updated", "textChannels"),
    UPDATE_PREMIUM("updated", "premiumKeys"),
    UPDATE_TWITTER("updated", "twitter"),
    UPDATE_RSS_FEEDS("rss_feeds", "rssFeeds"),

    RESET_XP("resetXp", "unknown")
}