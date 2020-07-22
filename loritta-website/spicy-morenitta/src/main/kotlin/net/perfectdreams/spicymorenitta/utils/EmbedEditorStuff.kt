package net.perfectdreams.spicymorenitta.utils

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.embededitor.data.crosswindow.Placeholder
import net.perfectdreams.loritta.embededitor.data.crosswindow.RenderType
import net.perfectdreams.loritta.utils.Placeholders

object EmbedEditorStuff {
    const val LORITTA_NAME = "Loritta"
    const val LORITTA_AS_MENTION = "@Loritta"
    const val LORITTA_DISCRIMINATOR = "0219"
    const val LORITTA_TAG = "$LORITTA_NAME#$LORITTA_DISCRIMINATOR"
    const val LORITTA_ID = "297153970613387264"
    const val LORITTA_AVATAR = "https://cdn.discordapp.com/avatars/297153970613387264/fd27e53031e4e600d06207f6853af908.png?size=2048"
    const val placeholdersPrefix = "website.dashboard.placeholders"
    const val placeholdersUserPrefix = "$placeholdersPrefix.user"

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
}