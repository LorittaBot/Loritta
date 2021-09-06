package net.perfectdreams.loritta.common.memory.entities

import net.perfectdreams.loritta.common.entities.UserProfile

class MemoryUserProfile(
    override val id: ULong,
    override val money: Long
) : UserProfile