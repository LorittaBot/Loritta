package net.perfectdreams.loritta.common.memory.services.memory

import net.perfectdreams.loritta.common.memory.services.Services

class MemoryServices : Services() {
    override val profiles = MemoryUserProfileService()
}