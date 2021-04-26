package net.perfectdreams.loritta.common.services.memory

import net.perfectdreams.loritta.common.services.Services

class MemoryServices : Services() {
    override val profiles = MemoryUserProfileService()
}