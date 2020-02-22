package net.perfectdreams.loritta.plugin.funky.tables

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import net.perfectdreams.loritta.plugin.funky.dao.MusicConfig
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object FakeTable : Table() {
	val musicConfig = ServerConfigs.optReference("music_config", MusicConfigs, onDelete = ReferenceOption.CASCADE)
}

var ServerConfig.musicConfig by MusicConfig optionalReferencedOn FakeTable.musicConfig