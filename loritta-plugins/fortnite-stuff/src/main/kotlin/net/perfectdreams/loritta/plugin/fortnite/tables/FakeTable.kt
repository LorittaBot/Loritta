package net.perfectdreams.loritta.plugin.fortnite.tables

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import net.perfectdreams.loritta.plugin.fortnite.dao.FortniteConfig
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object FakeTable : Table() {
	val fortniteConfig = ServerConfigs.optReference("fortnite_config", FortniteConfigs, onDelete = ReferenceOption.CASCADE).index()
}

var ServerConfig.fortniteConfig by FortniteConfig optionalReferencedOn FakeTable.fortniteConfig