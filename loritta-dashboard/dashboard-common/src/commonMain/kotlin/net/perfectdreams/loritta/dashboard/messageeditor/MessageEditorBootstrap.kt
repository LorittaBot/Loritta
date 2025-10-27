package net.perfectdreams.loritta.dashboard.messageeditor

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.dashboard.discord.DiscordGuild
import net.perfectdreams.loritta.dashboard.discordmessages.RenderableDiscordUser
import net.perfectdreams.loritta.placeholders.LorittaPlaceholder

@Serializable
data class MessageEditorBootstrap(
    val selfUser: RenderableDiscordUser,
    val templates: List<LorittaMessageTemplate>,
    val placeholders: List<MessageEditorMessagePlaceholderGroup>,
    val guild: DiscordGuild,
    val testMessageTarget: TestMessageTarget,
    val verifiedIconRawHtml: String,
    val eyeDropperIconRawHtml: String,
    val chevronDownIconRawHtml: String,
) {
    @Serializable
    sealed class TestMessageTarget {
        @Serializable
        class QuerySelector(val querySelector: String) : TestMessageTarget()
        @Serializable
        data object SendDirectMessage : TestMessageTarget()
        @Serializable
        data object Unavailable : TestMessageTarget()
    }
}

@Serializable
data class LorittaMessageTemplate(
    val name: String,
    val content: String
)

@Serializable
data class MessageEditorMessagePlaceholderGroup(
    val placeholders: List<LorittaPlaceholder>,
    val description: String?,
    // Yes, this means that technically someone could change what replacer is being used when testing a message on the dashboard
    // But does it really matter? The user can already type whatever they want in the box...
    val replaceWithBackend: String,
    val replaceWithFrontend: String,
    val renderType: RenderType
) {
    enum class RenderType {
        TEXT,
        MENTION,
    }
}