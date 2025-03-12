package net.perfectdreams.loritta.morenitta.utils

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.requests.RestAction

/**
 * A user private channel from a cached channel ID
 *
 * Private Channel IDs never change, so it is beneficial to cache the channel IDs
 */
class CachedPrivateChannel(private val jda: JDA, val cachedChannelId: Long) : PrivateChannel {
    override fun getUser(): User? {
        return null
    }

    override fun retrieveUser(): RestAction<User?> {
        error("Cannot retrieve a user from a cached private channel!")
    }

    override fun getName(): String {
        error("Cannot get the name of a cached private channel!")
    }

    override fun getLatestMessageIdLong(): Long {
        error("Cannot get the latest message ID of a cached private channel!")
    }

    override fun canTalk(): Boolean {
        return true
    }

    override fun getType(): ChannelType {
        return ChannelType.PRIVATE
    }

    override fun getJDA(): JDA {
        return this.jda
    }

    override fun delete(): RestAction<Void?> {
        error("Cannot delete a cached private channel!")
    }

    override fun getIdLong(): Long {
        return this.cachedChannelId
    }

    override fun isDetached(): Boolean {
        return false
    }
}