package net.perfectdreams.loritta.cinnamon.platform.interaktions.entities

import net.perfectdreams.discordinteraktions.api.entities.Snowflake
import net.perfectdreams.discordinteraktions.common.context.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.utils.AllowedMentions
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaMessage
import net.perfectdreams.loritta.cinnamon.common.entities.MessageChannel

abstract class InteraKTionsMessageChannel(private val context: ApplicationCommandContext) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        if (message.isEphemeral) {
            // Keep in mind that ephemeral messages do not support *everything*, so let's throw a exception if
            // we are using stuff that Discord ephemeral messages do not support
            if (message.files.isNotEmpty())
                throw UnsupportedOperationException("Ephemeral Messages do not support files!")

            context.sendEphemeralMessage {
                val impersonation = message.impersonation

                if (impersonation != null) {
                    // We are going to *impersonate* someone (woo)
                    // Because we can't use webhooks, we will replace them with a embed
                    embed {
                        author(impersonation.username, null, impersonation.avatar.url)
                        description = message.content
                    }
                } else {
                    content = buildString {
                        if (message.content != null)
                            append(message.content)

                        for (reply in message.replies) {
                            append("\n")
                            append("${reply.prefix} **|** ${reply.content}")
                        }
                    }
                }

                message.embeds?.forEach {
                    embed {
                        title = it.title
                        description = it.description
                        url = it.url

                        val color = it.color
                        val author = it.author
                        val image = it.image
                        val thumbnail = it.thumbnail
                        val footer = it.footer
                        val fields = it.fields

                        if (color != null)
                            color(color.rgb)

                        if (author != null)
                            author(author.name, author.url, author.iconUrl)

                        if (image != null)
                            image(image.url)

                        if (thumbnail != null)
                            thumbnail(thumbnail.url)

                        if (footer != null)
                            footer(footer.text, footer.iconUrl)

                        fields.forEach {
                            field(
                                it.name,
                                it.value,
                                it.inline
                            )
                        }
                    }
                }

                // Allowed Mentions
                // By default we will disable ALL mentions, to avoid a "@everyone moment 3.0"
                // Messages can enable specific mentions if needed
                allowedMentions = AllowedMentions(
                    users = message.allowedMentions.users.map { Snowflake(it.id) },
                    roles = listOf(), // TODO: Add support to this
                    repliedUser = message.allowedMentions.repliedUser
                )
            }
        } else {
            context.sendMessage {
                val impersonation = message.impersonation

                if (impersonation != null) {
                    // We are going to *impersonate* someone (woo)
                    // Because we can't use webhooks, we will replace them with a embed
                    embed {
                        author(impersonation.username, null, impersonation.avatar.url)
                        description = message.content
                    }
                } else {
                    content = buildString {
                        if (message.content != null)
                            append(message.content)

                        for (reply in message.replies) {
                            append("\n")
                            append("${reply.prefix} **|** ${reply.content}")
                        }
                    }
                }

                for (file in message.files) {
                    file(file.key, file.value.inputStream())
                }

                message.embeds?.forEach {
                    embed {
                        title = it.title
                        description = it.description
                        url = it.url

                        val color = it.color
                        val author = it.author
                        val image = it.image
                        val thumbnail = it.thumbnail
                        val footer = it.footer
                        val fields = it.fields

                        if (color != null)
                            color(color.rgb)

                        if (author != null)
                            author(author.name, author.url, author.iconUrl)

                        if (image != null)
                            image(image.url)

                        if (thumbnail != null)
                            thumbnail(thumbnail.url)

                        if (footer != null)
                            footer(footer.text, footer.iconUrl)

                        fields.forEach {
                            field(
                                it.name,
                                it.value,
                                it.inline
                            )
                        }
                    }
                }

                // Allowed Mentions
                // By default we will disable ALL mentions, to avoid a "@everyone moment 3.0"
                // Messages can enable specific mentions if needed
                allowedMentions = AllowedMentions(
                    users = message.allowedMentions.users.map { Snowflake(it.id) },
                    roles = listOf(), // TODO: Add support to this
                    repliedUser = message.allowedMentions.repliedUser
                )
            }
        }
    }
}