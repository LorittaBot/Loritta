package net.perfectdreams.loritta.platform.twitter.entities

import net.perfectdreams.loritta.api.entities.User

class TwitterUser(val user: twitter4j.User) : User {
    override val name: String
        get() = user.name
    override val avatar: String?
        get() = user.profileImageURL
    override val avatarUrl: String?
        get() = user.profileImageURL
    override val isBot: Boolean
        get() = false
    override val asMention: String
        get() = "@${user.screenName}"
    override val id: Long
        get() = user.id
}