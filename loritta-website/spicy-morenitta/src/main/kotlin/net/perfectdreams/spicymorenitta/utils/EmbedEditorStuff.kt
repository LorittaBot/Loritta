package net.perfectdreams.spicymorenitta.utils

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.embededitor.data.crosswindow.Placeholder
import net.perfectdreams.loritta.embededitor.data.crosswindow.RenderType
import net.perfectdreams.loritta.utils.Placeholders
import kotlinx.browser.window

object EmbedEditorStuff {
    const val LORITTA_NAME = "Loritta"
    const val LORITTA_AS_MENTION = "@Loritta"
    const val LORITTA_DISCRIMINATOR = "0219"
    const val LORITTA_TAG = "$LORITTA_NAME#$LORITTA_DISCRIMINATOR"
    const val LORITTA_ID = "297153970613387264"
    val LORITTA_AVATAR = "${window.location.origin}/assets/img/lori_avatar_v4.png"
    const val EXPERIENCE_LEVEL = "100"
    const val EXPERIENCE_XP = "100002"
    const val EXPERIENCE_RANKING = "5"
    const val EXPERIENCE_NEXT_LEVEL = "101"
    const val EXPERIENCE_NEXT_LEVEL_REQUIRED_XP = "998"
    const val EXPERIENCE_NEXT_LEVEL_TOTAL_XP = "101000"
    const val PUNISHMENT_REASON = "You're gonna have a bad time"
    const val PUNISHMENT_TYPE = "Banned"

    const val placeholdersPrefix = "website.dashboard.placeholders"
    const val placeholdersUserPrefix = "$placeholdersPrefix.user"
    const val placeholdersExperiencePrefix = "$placeholdersPrefix.experience"
    const val placeholdersStaffPrefix = "$placeholdersPrefix.staff"
    const val placeholdersPunishmentPrefix = "$placeholdersPrefix.punishment"

    fun userInContextPlaceholders(locale: BaseLocale) = listOf(
            Placeholder(
                    Placeholders.USER_MENTION.asKey,
                    LORITTA_AS_MENTION,
                    locale["$placeholdersUserPrefix.mention"],
                    RenderType.MENTION,
                    false
            ),
            Placeholder(
                    Placeholders.USER_NAME_SHORT.asKey,
                    LORITTA_NAME,
                    locale["$placeholdersUserPrefix.name"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.USER_NAME.asKey,
                    LORITTA_NAME,
                    locale["$placeholdersUserPrefix.name"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.USER_DISCRIMINATOR.asKey,
                    LORITTA_DISCRIMINATOR,
                    locale["$placeholdersUserPrefix.discriminator"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.USER_TAG.asKey,
                    LORITTA_TAG,
                    locale["$placeholdersUserPrefix.tag"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.USER_ID.asKey,
                    LORITTA_ID,
                    locale["$placeholdersUserPrefix.id"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.USER_AVATAR_URL.asKey,
                    LORITTA_AVATAR,
                    locale["$placeholdersUserPrefix.avatarUrl"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.USER_NICKNAME.asKey,
                    LORITTA_NAME,
                    locale["$placeholdersUserPrefix.nickname"],
                    RenderType.TEXT,
                    false
            ),

            // === [ DEPRECATED ] ===
            Placeholder(
                    Placeholders.Deprecated.USER_ID.asKey, // Deprecated
                    LORITTA_ID,
                    null,
                    RenderType.TEXT,
                    true
            ),
            Placeholder(
                    Placeholders.Deprecated.USER_DISCRIMINATOR.asKey, // Deprecated
                    LORITTA_DISCRIMINATOR,
                    null,
                    RenderType.TEXT,
                    true
            ),
            Placeholder(
                    Placeholders.Deprecated.USER_NICKNAME.asKey, // Deprecated
                    LORITTA_NAME,
                    null,
                    RenderType.TEXT,
                    true
            ),
            Placeholder(
                    Placeholders.Deprecated.USER_AVATAR_URL.asKey,
                    LORITTA_AVATAR,
                    null,
                    RenderType.TEXT,
                    true
            )
    )

    fun userCurrentExperienceInContextPlaceholders(locale: BaseLocale) = listOf(
            Placeholder(
                    Placeholders.EXPERIENCE_LEVEL_SHORT.asKey,
                    EXPERIENCE_LEVEL,
                    locale["$placeholdersExperiencePrefix.level"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.EXPERIENCE_XP_SHORT.asKey,
                    EXPERIENCE_XP,
                    locale["$placeholdersExperiencePrefix.xp"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.EXPERIENCE_RANKING.asKey,
                    EXPERIENCE_RANKING,
                    locale["$placeholdersExperiencePrefix.ranking"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.EXPERIENCE_NEXT_LEVEL.asKey,
                    EXPERIENCE_NEXT_LEVEL,
                    locale["$placeholdersExperiencePrefix.nextLevel"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.EXPERIENCE_NEXT_LEVEL_TOTAL_XP.asKey,
                    EXPERIENCE_NEXT_LEVEL_TOTAL_XP,
                    locale["$placeholdersExperiencePrefix.nextLevelTotalXp"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.EXPERIENCE_NEXT_LEVEL_REQUIRED_XP.asKey,
                    EXPERIENCE_NEXT_LEVEL_REQUIRED_XP,
                    locale["$placeholdersExperiencePrefix.nextLevelRequiredXp"],
                    RenderType.TEXT,
                    false
            )
    )

    fun staffInContextPlaceholders(locale: BaseLocale) = listOf(
            Placeholder(
                    Placeholders.STAFF_MENTION.asKey,
                    LORITTA_AS_MENTION,
                    locale["$placeholdersStaffPrefix.mention"],
                    RenderType.MENTION,
                    false
            ),
            Placeholder(
                    Placeholders.STAFF_NAME_SHORT.asKey,
                    LORITTA_NAME,
                    locale["$placeholdersStaffPrefix.name"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.STAFF_NAME.asKey,
                    LORITTA_NAME,
                    locale["$placeholdersStaffPrefix.name"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.STAFF_DISCRIMINATOR.asKey,
                    LORITTA_DISCRIMINATOR,
                    locale["$placeholdersStaffPrefix.discriminator"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.STAFF_TAG.asKey,
                    LORITTA_TAG,
                    locale["$placeholdersStaffPrefix.tag"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.STAFF_ID.asKey,
                    LORITTA_ID,
                    locale["$placeholdersStaffPrefix.id"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.STAFF_AVATAR_URL.asKey,
                    LORITTA_AVATAR,
                    locale["$placeholdersStaffPrefix.avatarUrl"],
                    RenderType.TEXT,
                    false
            ),
            /* Placeholder(
                    Placeholders.STAFF_NICKNAME.asKey,
                    LORITTA_NAME,
                    locale["$placeholdersStaffPrefix.nickname"],
                    RenderType.TEXT,
                    false
            ), */

            // === [ DEPRECATED ] ===
            Placeholder(
                    Placeholders.Deprecated.STAFF_ID.asKey, // Deprecated
                    LORITTA_ID,
                    null,
                    RenderType.TEXT,
                    true
            ),
            Placeholder(
                    Placeholders.Deprecated.STAFF_DISCRIMINATOR.asKey, // Deprecated
                    LORITTA_DISCRIMINATOR,
                    null,
                    RenderType.TEXT,
                    true
            ),
            Placeholder(
                    Placeholders.Deprecated.STAFF_AVATAR_URL.asKey,
                    LORITTA_AVATAR,
                    null,
                    RenderType.TEXT,
                    true
            )
    )

    fun punishmentInContextPlaceholders(locale: BaseLocale) = listOf(
            Placeholder(
                    Placeholders.PUNISHMENT_REASON.asKey,
                    PUNISHMENT_REASON,
                    locale["$placeholdersPunishmentPrefix.reason"],
                    RenderType.MENTION,
                    false
            ),
            Placeholder(
                    Placeholders.PUNISHMENT_REASON_SHORT.asKey,
                    PUNISHMENT_REASON,
                    locale["$placeholdersPunishmentPrefix.reason"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.PUNISHMENT_TYPE.asKey,
                    PUNISHMENT_TYPE,
                    locale["$placeholdersPunishmentPrefix.type"],
                    RenderType.TEXT,
                    false
            ),
            Placeholder(
                    Placeholders.PUNISHMENT_TYPE_SHORT.asKey,
                    PUNISHMENT_TYPE,
                    locale["$placeholdersPunishmentPrefix.type"],
                    RenderType.TEXT,
                    false
            )
    )
}