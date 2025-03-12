package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundVariations
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.serializable.BackgroundVariation
import org.jetbrains.exposed.sql.selectAll

class BackgroundsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getBackground(internalName: String) = pudding.transaction {
        Backgrounds.selectFirstOrNull {
            Backgrounds.internalName eq internalName
        }?.let { PuddingBackground.fromRow(it) }
    }

    suspend fun getBackgroundVariations(internalName: String) = pudding.transaction {
        BackgroundVariations
            .selectAll().where { BackgroundVariations.background eq internalName }
            .map { BackgroundVariation.fromRow(it) }
    }
}