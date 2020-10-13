package com.mrpowergamerbr.loritta.dao

import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.serializable.Crop
import net.perfectdreams.loritta.tables.Backgrounds
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Background(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, Background>(Backgrounds) {
		const val DEFAULT_BACKGROUND_ID = "defaultBlue"
		const val RANDOM_BACKGROUND_ID = "random"
		const val CUSTOM_BACKGROUND_ID = "custom"
	}

	var imageFile by Backgrounds.imageFile
	var enabled by Backgrounds.enabled
	var rarity by Backgrounds.rarity
	var createdBy by Backgrounds.createdBy
	var availableToBuyViaDreams by Backgrounds.availableToBuyViaDreams
	var availableToBuyViaMoney by Backgrounds.availableToBuyViaMoney
	var crop by Backgrounds.crop
	var set by Backgrounds.set

	fun toSerializable() = net.perfectdreams.loritta.serializable.Background(
			id.value,
			imageFile,
			enabled,
			rarity,
			createdBy.toList(),
			crop?.let { Json.decodeFromString(Crop.serializer(), it.toString()) },
			set?.value
	)
}