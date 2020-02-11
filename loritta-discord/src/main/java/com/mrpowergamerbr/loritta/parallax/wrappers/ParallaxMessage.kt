package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import org.graalvm.polyglot.Value
import java.util.function.Function

class ParallaxMessage(private val message: Message) {
    val attachments = message.attachments.map { ParallaxAttachment(it) }
    val author = ParallaxUser(message.author)
    val channel = ParallaxTextChannel(message.textChannel)
    val cleanContent get() = message.contentStripped
    val client = ParallaxClient(message.jda)
    val content get() = message.contentDisplay
    val createdAt get() = message.timeCreated
    // TODO: createdTimestamp
    val deletable get() = message.author.id == loritta.discordConfig.discord.clientId || message.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)
    val editable get() = message.author.id == loritta.discordConfig.discord.clientId
    val editedAt get() = message.timeEdited
    // TODO: editedTimestamp
    // TODO: edits
    // TODO: embeds
    val embed: ParallaxEmbed? = null
    val guild = ParallaxGuild(message.guild)
    // TODO: hit
    val id get() = message.id
    val member get() = ParallaxMember(message.member!!)
    // TODO: mentions
    // TODO: nonce
    val pinnable get() = message.author.id == loritta.discordConfig.discord.clientId
    val pinned get() = message.isPinned
    // TODO: reactions
    // TODO: system
    val tts get() = message.isTTS
    // TODO: type
    // TODO: webhookID

    // TODO: awaitReactions

    fun clearReactions() {
        message.clearReactions().complete()
    }

    // TODO: createReactionCollector

    fun delete() {
        message.delete().complete()
    }

    fun edit(content: String) {
        message.editMessage(content).complete()
    }

    fun edit(embed: ParallaxEmbed) {
        message.editMessage(embed.toDiscordEmbed()).complete()
    }

    fun edit(mirror: Value) {
        edit(ParallaxUtils.toParallaxEmbed(mirror))
    }

    // TODO: isMemberMentioned
    // TODO: isMentioned

    fun pin(): ParallaxPromise<Void?> {
        return object: ParallaxPromise<Void?>() {
            override fun queue(success: Function<Void?, Any?>?, failure: Function<Any?, Any?>?) {
                message.pin().queue({
                    success?.apply(null)
                }, {
                    if (failure == null)
                        ParallaxPromise.DEFAULT_CHANNEL_FAILURE_CALLBACK.invoke(message.channel, it)
                    else
                        failure.apply(null)
                })
            }
        }
    }

    fun unpin(): ParallaxPromise<Void?> {
        return object: ParallaxPromise<Void?>() {
            override fun queue(success: Function<Void?, Any?>?, failure: Function<Any?, Any?>?) {
                message.unpin().queue({
                    success?.apply(null)
                }, {
                    if (failure == null)
                        ParallaxPromise.DEFAULT_CHANNEL_FAILURE_CALLBACK.invoke(message.channel, it)
                    else
                        failure.apply(null)
                })
            }
        }
    }

    fun react(reaction: String): ParallaxPromise<Void?> {
        return object: ParallaxPromise<Void?>() {
            override fun queue(success: Function<Void?, Any?>?, failure: Function<Any?, Any?>?) {
                message.addReaction(reaction).queue({
                    success?.apply(null)
                }, {
                    if (failure == null)
                        ParallaxPromise.DEFAULT_CHANNEL_FAILURE_CALLBACK.invoke(message.channel, it)
                    else
                        failure.apply(null)
                })
            }
        }
    }

    fun reply(content: Any) {
        if (content is Value || content is Map<*, *> || content is ParallaxEmbed) {
            channel.send(content)
        } else {
            channel.send(author.toString() + " " + content)
        }
    }

    override fun toString(): String = content
}