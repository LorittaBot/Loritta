package net.perfectdreams.loritta.common.utils

// Yes, this is similar to the PunishmentAction file
// This is a duplicate because the PunishmentAction is used for other things that we don't have moderation messages
// associated with it yet
enum class ModerationLogAction {
    BAN,
    KICK,
    MUTE,
    WARN,
    UNBAN,
    UNMUTE,
    UNWARN
}
