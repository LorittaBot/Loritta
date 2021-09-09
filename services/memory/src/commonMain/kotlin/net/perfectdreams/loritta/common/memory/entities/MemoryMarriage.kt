package net.perfectdreams.loritta.cinnamon.common.memory.entities

import kotlinx.datetime.Instant
import net.perfectdreams.loritta.cinnamon.common.entities.Marriage

class MemoryMarriage(
    override val id: Long,
    override val user1: Long,
    override val user2: Long,
    override val marriedSince: Instant
) : Marriage