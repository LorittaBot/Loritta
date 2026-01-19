package net.perfectdreams.loritta.dashboard.frontend.compose.components.messages

import net.perfectdreams.loritta.dashboard.discordmessages.DiscordComponent
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordEmbed
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.dashboard.frontend.compose.components.messages.MutableDiscordMessage.MutableDiscordComponent.*

class MutableDiscordMessage(
    source: DiscordMessage,
    private val onMessageContentChange: (String) -> (Unit)
) {
    companion object {
        private fun transformIntoMutable(component: DiscordComponent) = when (component) {
            is DiscordComponent.DiscordActionRow -> {
                MutableActionRow(component)
            }
            is DiscordComponent.DiscordButton -> {
                MutableButton(component)
            }

            is DiscordComponent.DiscordContainer -> {
                MutableContainer(component)
            }
            is DiscordComponent.DiscordMediaGallery -> MutableMediaGallery(component)
            is DiscordComponent.DiscordSection -> MutableSection(component)
            is DiscordComponent.DiscordSeparator -> MutableSeparator(component)
            is DiscordComponent.DiscordTextDisplay -> MutableTextDisplay(component)
            is DiscordComponent.DiscordThumbnail -> MutableThumbnail(component)
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

                is MutableDiscordComponent.MutableContainer -> DiscordComponent.DiscordContainer(
                    components = component.components.map {
                        transformToData(it)
                    },
                    accentColor = component.accentColor,
                    spoiler = component.spoiler
                )

                is MutableDiscordComponent.MutableMediaGallery -> DiscordComponent.DiscordMediaGallery(
                    items = component.items.map {
                        DiscordComponent.DiscordMediaGallery.MediaGalleryItem(
                            media = DiscordComponent.UnfurledMediaItem(
                                url = it.media.url
                            ),
                            description = it.description,
                            spoiler = it.spoiler
                        )
                    }
                )

                is MutableDiscordComponent.MutableSection -> DiscordComponent.DiscordSection(
                    components = component.components.map {
                        transformToData(it)
                    },
                    accessory = component.accessory?.let { transformToData(it) }
                )

                is MutableDiscordComponent.MutableSeparator -> DiscordComponent.DiscordSeparator(
                    divider = component.divider,
                    spacing = component.spacing
                )

                is MutableDiscordComponent.MutableTextDisplay -> DiscordComponent.DiscordTextDisplay(
                    content = component.content
                )

                is MutableDiscordComponent.MutableThumbnail -> DiscordComponent.DiscordThumbnail(
                    media = DiscordComponent.UnfurledMediaItem(
                        url = component.media.url
                    ),
                    description = component.description,
                    spoiler = component.spoiler
                )
            }
        }
    }

    var content = source.content
    var embeds = source.embeds?.map { MutableDiscordEmbed(it) }?.toMutableList() ?: mutableListOf()
    val components = source.components?.map { transformIntoMutable(it) }?.toMutableList() ?: mutableListOf()
    var flags = source.flags

    fun isComponentsV2() = flags and (1 shl 15) != 0
    fun setComponentsV2Mode(enabled: Boolean) {
        flags = if (enabled) {
            flags or (1 shl 15)
        } else {
            flags and (1 shl 15).inv()
        }
    }

    fun transformToData() = DiscordMessage(
        content = if (isComponentsV2()) null else content,
        embeds = if (isComponentsV2()) null else embeds.map {
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
        }.ifEmpty { null },
        flags = flags
    )

    /**
     * Transforms the current [MutableDiscordMessage] into a [DiscordMessage], encodes it in JSON, and invokes [onMessageContentChange]
     */
    fun triggerUpdate() {
        try {
            onMessageContentChange.invoke(DiscordMessage.JsonForDiscordMessages.encodeToString(transformToData()))
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

        class MutableContainer(source: DiscordComponent.DiscordContainer) : MutableDiscordComponent() {
            var accentColor = source.accentColor
            var spoiler = source.spoiler
            val components = source.components.map { transformIntoMutable(it) }.toMutableList()
        }

        class MutableMediaGallery(source: DiscordComponent.DiscordMediaGallery) : MutableDiscordComponent() {
            val items = source.items.map { MutableMediaGalleryItem(it) }.toMutableList()

            class MutableMediaGalleryItem(source: DiscordComponent.DiscordMediaGallery.MediaGalleryItem) {
                var media = MutableUnfurledMediaItem(source.media)
                var description = source.description
                var spoiler = source.spoiler
            }
        }

        class MutableUnfurledMediaItem(source: DiscordComponent.UnfurledMediaItem) {
            var url = source.url
        }

        class MutableSection(source: DiscordComponent.DiscordSection) : MutableDiscordComponent() {
            val components = source.components.map { transformIntoMutable(it) }.toMutableList()
            var accessory = source.accessory?.let { transformIntoMutable(it) }
        }

        class MutableSeparator(source: DiscordComponent.DiscordSeparator) : MutableDiscordComponent() {
            var divider = source.divider
            var spacing = source.spacing
        }

        class MutableTextDisplay(source: DiscordComponent.DiscordTextDisplay) : MutableDiscordComponent() {
            var content = source.content
        }

        class MutableThumbnail(source: DiscordComponent.DiscordThumbnail) : MutableDiscordComponent() {
            var media = MutableUnfurledMediaItem(source.media)
            var description = source.description
            var spoiler = source.spoiler
        }
    }
}