package net.perfectdreams.loritta.legacy.common.builder

import net.perfectdreams.loritta.legacy.common.entities.AllowedMentions
import net.perfectdreams.loritta.legacy.common.entities.User
import net.perfectdreams.loritta.legacy.common.utils.CinnamonDslMarker

@CinnamonDslMarker
class AllowedMentionsBuilder {
    val users = mutableSetOf<User>()
    var repliedUser = true

    fun build() = AllowedMentions(users, repliedUser)
}