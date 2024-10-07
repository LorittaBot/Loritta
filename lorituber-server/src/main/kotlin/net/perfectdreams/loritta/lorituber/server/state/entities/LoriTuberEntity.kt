package net.perfectdreams.loritta.lorituber.server.state.entities

abstract class LoriTuberEntity {
    /**
     * Indicates that the entity data is dirty and must be persisted to the disk
     */
    var isDirty = false

    /**
     * Indicates that the entity has been removed and should be removed from the memory on the next persistence cycle
     */
    var isRemoved = false

    /**
     * Indicates that the entity is ephemeral and should not be persisted to the disk
     */
    var isEphemeral = false
}