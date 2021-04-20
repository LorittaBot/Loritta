package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.entities.AllowedMentions
import net.perfectdreams.loritta.common.entities.User

class AllowedMentionsBuilder {
    val users = mutableSetOf<User>()
    var repliedUser = true

    fun build() = AllowedMentions(users, repliedUser)
}