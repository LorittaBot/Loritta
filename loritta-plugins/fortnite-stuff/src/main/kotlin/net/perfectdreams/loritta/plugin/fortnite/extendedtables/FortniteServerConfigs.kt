package net.perfectdreams.loritta.plugin.fortnite.extendedtables

import com.mrpowergamerbr.loritta.tables.SnowflakeTable

object FortniteServerConfigs : SnowflakeTable("serverconfigs") {
	val fortniteConfig = optReference("fortnite_config", FortniteConfigs)
}