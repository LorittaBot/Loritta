package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.optional.Optional
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.json.request.EmbedRequest
import dev.kord.rest.json.request.MessageCreateRequest
import dev.kord.rest.json.request.MultipartMessageCreateRequest
import kotlinx.serialization.Serializable
import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
data class ImportantNotificationDatabaseMessage(
    val embeds: List<EmbedRequest>
) {
    fun toMultipartMessageCreateRequest() = MultipartMessageCreateRequest(toMessageCreateRequest())

    fun toMessageCreateRequest() = MessageCreateRequest(
        embeds = Optional(embeds)
    )

    fun toPublicInteractionOrFollowupMessageCreateBuilder() = toInteractionOrFollowupMessageCreateBuilder(false)
    fun toEphemeralInteractionOrFollowupMessageCreateBuilder() = toInteractionOrFollowupMessageCreateBuilder(true)

    // TODO: We need to make this easier on the Discord InteraKTions side of things
    fun toInteractionOrFollowupMessageCreateBuilder(ephemeral: Boolean) = InteractionOrFollowupMessageCreateBuilder(ephemeral)
        .apply {
            this@ImportantNotificationDatabaseMessage.embeds.forEach {
                embed {
                    it.author.value?.let {
                        author(it.name.value!!, it.url.value, it.iconUrl.value)
                    }
                    this.title = it.title.value
                    this.description = it.description.value
                    this.image = it.image.value?.url
                    this.thumbnailUrl = it.thumbnail.value?.url
                    this.color = it.color.value
                    this.timestamp = it.timestamp.value
                    it.fields.value?.forEach {
                        field(it.name, it.value, it.inline.discordBoolean)
                    }
                }
            }
        }
}

// TODO: Remove this
class ImportantNotificationDatabaseMessageBuilder {
    var content: String? = null

    var tts: Boolean? = null

    var embeds: MutableList<EmbedBuilder>? = mutableListOf()

    var allowedMentions: AllowedMentionsBuilder? = null

    var components: MutableList<MessageComponentBuilder>? = mutableListOf()

    fun toMessage() = ImportantNotificationDatabaseMessage(
        embeds?.map { it.toRequest() } ?: listOf()
    )
}

@OptIn(ExperimentalContracts::class)
inline fun ImportantNotificationDatabaseMessageBuilder.embed(block: EmbedBuilder.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    embeds = (embeds ?: mutableListOf()).also {
        it.add(EmbedBuilder().apply(block))
    }
}