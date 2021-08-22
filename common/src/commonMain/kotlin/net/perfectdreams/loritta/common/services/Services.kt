package net.perfectdreams.loritta.common.services

abstract class Services {
    abstract val users: UserService
    abstract val serverConfigs: ServerConfigsService
    abstract val marriages: MarriagesService
    abstract val shipEffects: ShipEffectsService
    abstract val sonhos: SonhosService
}