package net.perfectdreams.loritta.dashboard.frontend.compose.components.messages

import net.perfectdreams.loritta.dashboard.discordmessages.DiscordComponent
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordEmbed
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage

class MutableDiscordMessage(
    source: DiscordMessage,
    private val onMessageContentChange: (String) -> (Unit)
) {
    companion object {
        private fun transformIntoMutable(component: DiscordComponent) = when (component) {
            is DiscordComponent.DiscordActionRow -> {
                MutableDiscordComponent.MutableActionRow(component)
            }
            is DiscordComponent.DiscordButton -> {
                MutableDiscordComponent.MutableButton(component)
            }
        }

        private fun transformToData(component: MutableDiscordComponent): DiscordComponent {
            return when (component) {
                is MutableDiscordComponent.MutableActionRow -> DiscordComponent.DiscordActionRow(
                    components = component.components.map {
                        transformToData(it)
                    }
                )
                is MutableDiscordComponent.MutableButton -> DiscordComponent.DiscordButton(
                    label = component.label ?: "",
                    style = 5,
                    url = component.url ?: ""
                )
            }
        }
    }

    var content = source.content
    var embeds = source.embeds?.map { MutableDiscordEmbed(it) }?.toMutableList() ?: mutableListOf()
    val components = source.components?.map { transformIntoMutable(it) }?.toMutableList() ?: mutableListOf()

    fun transformToData() = DiscordMessage(
        content,
        embeds = embeds.map {
            DiscordEmbed(
                author = it.author?.let {
                    DiscordEmbed.Author(it.name!!, it.url, it.iconUrl)
                },
                title = it.title,
                url = it.url,
                description = it.description,
                color = it.color,
                fields = it.fields.map {
                    DiscordEmbed.Field(
                        it.name,
                        it.value,
                        it.inline
                    )
                },
                image = it.imageUrl?.let { DiscordEmbed.EmbedUrl(it) },
                thumbnail = it.thumbnailUrl?.let { DiscordEmbed.EmbedUrl(it) },
                footer = it.footer?.let {
                    DiscordEmbed.Footer(it.text!!, it.iconUrl)
                }
            )
        },
        components = components.map {
            transformToData(it)
        }.ifEmpty { null }
    )

    /**
     * Transforms the current [MutableDiscordMessage] into a [DiscordMessage], encodes it in JSON, and invokes [onMessageContentChange]
     */
    fun triggerUpdate() {
        try {
            onMessageContentChange.invoke(JsonForDiscordMessages.encodeToString(transformToData()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class MutableDiscordEmbed(source: DiscordEmbed) {
        var author = source.author?.let { MutableAuthor(it) }
        var title = source.title
        var url = source.url
        var description = source.description
        var color = source.color
        var fields = source.fields.map { MutableField(it) }.toMutableList()
        var imageUrl = source.image?.url
        var thumbnailUrl = source.thumbnail?.url
        var footer = source.footer?.let { MutableFooter(it) }

        class MutableField(source: DiscordEmbed.Field) {
            var name = source.name
            var value = source.value
            var inline = source.inline
        }

        class MutableAuthor(source: DiscordEmbed.Author) {
            var name: String? = source.name
            var url: String? = source.url
            var iconUrl: String? = source.iconUrl
        }

        class MutableFooter(source: DiscordEmbed.Footer) {
            var text: String? = source.text
            var iconUrl: String? = source.iconUrl
        }
    }

    sealed class MutableDiscordComponent {
        class MutableActionRow(source: DiscordComponent.DiscordActionRow) : MutableDiscordComponent() {
            val components = source.components.map { transformIntoMutable(it) }.toMutableList()
        }

        class MutableButton(source: DiscordComponent.DiscordButton) : MutableDiscordComponent() {
            var label: String? = source.label
            var url: String? = source.url
        }
    }
}