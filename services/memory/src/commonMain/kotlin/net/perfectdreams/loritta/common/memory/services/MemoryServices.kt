package net.perfectdreams.loritta.common.memory.services

import net.perfectdreams.loritta.common.services.Services

class MemoryServices : Services() {
    override val users = MemoryUserService()
    override val marriages = MemoryMarriagesService()
    override val shipEffects = MemoryShipEffectsService()
    override val sonhos = MemorySonhosService(users)
    override val serverConfigs = MemoryServerConfigsService()
}