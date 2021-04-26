package net.perfectdreams.loritta.platform.cli.entities

import net.perfectdreams.loritta.common.entities.UserProfile

class CLIUserProfile(override val id: Long) : UserProfile {
    override val money: Long
        get() = TODO("Not yet implemented")
}