package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.modules.register.RegisterHolder
import com.mrpowergamerbr.loritta.utils.exposed.jsonb

object RegisterConfigs : SnowflakeTable() {
	val holder = jsonb("holder", RegisterHolder::class.java, Loritta.GSON)
}