package com.mrpowergamerbr.loritta.dao

import net.perfectdreams.loritta.tables.Backgrounds
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class Background(id: EntityID<String>) : Entity<String>(id) {
	companion object : EntityClass<String, Background>(Backgrounds)

	var imageFile by Backgrounds.imageFile
	var enabled by Backgrounds.enabled
	var rarity by Backgrounds.rarity
	var createdBy by Backgrounds.createdBy
	var availableToBuyViaDreams by Backgrounds.availableToBuyViaDreams
	var availableToBuyViaMoney by Backgrounds.availableToBuyViaMoney
	var crop by Backgrounds.crop
	var set by Backgrounds.set
}