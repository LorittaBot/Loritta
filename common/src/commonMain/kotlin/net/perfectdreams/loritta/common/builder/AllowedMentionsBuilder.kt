package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.entities.AllowedMentions
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.utils.CinnamonDslMarker

@CinnamonDslMarker
class AllowedMentionsBuilder {
    val users = mutableSetOf<User>()
    var repliedUser = true

    fun build() = AllowedMentions(users, repliedUser)
}