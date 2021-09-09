package net.perfectdreams.loritta.cinnamon.common.builder

import net.perfectdreams.loritta.cinnamon.common.entities.AllowedMentions
import net.perfectdreams.loritta.cinnamon.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.utils.CinnamonDslMarker

@CinnamonDslMarker
class AllowedMentionsBuilder {
    val users = mutableSetOf<User>()
    var repliedUser = true

    fun build() = AllowedMentions(users, repliedUser)
}