package net.perfectdreams.loritta.plugin.fortnite.extendedtables

import com.mrpowergamerbr.loritta.tables.SnowflakeTable
import org.jetbrains.exposed.sql.ReferenceOption

object FortniteServerConfigs : SnowflakeTable("serverconfigs") {
	val fortniteConfig = optReference("fortnite_config", FortniteConfigs, onDelete = ReferenceOption.CASCADE)
}