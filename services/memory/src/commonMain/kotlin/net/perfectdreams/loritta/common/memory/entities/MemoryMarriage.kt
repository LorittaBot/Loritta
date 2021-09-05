package net.perfectdreams.loritta.common.memory.entities

import kotlinx.datetime.Instant
import net.perfectdreams.loritta.common.entities.Marriage

class MemoryMarriage(
    override val id: Long,
    override val user1: Long,
    override val user2: Long,
    override val marriedSince: Instant
) : Marriage