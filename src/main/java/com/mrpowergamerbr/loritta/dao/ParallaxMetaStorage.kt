package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.ParallaxMetaStorages
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class ParallaxMetaStorage(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, ParallaxMetaStorage>(ParallaxMetaStorages)

	var guildId by ParallaxMetaStorages.guildId
	var storageName by ParallaxMetaStorages.storageName
	var data by ParallaxMetaStorages.data
}