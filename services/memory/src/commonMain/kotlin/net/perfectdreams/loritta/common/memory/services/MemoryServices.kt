package net.perfectdreams.loritta.common.memory.services

import net.perfectdreams.loritta.common.memory.services.MemoryUserProfileService
import net.perfectdreams.loritta.common.services.Services

class MemoryServices : Services() {
    override val profiles = MemoryUserProfileService()
}