package net.perfectdreams.loritta.legacy.tables.servers

import net.perfectdreams.loritta.legacy.tables.ServerConfigs
import net.perfectdreams.loritta.legacy.serializable.CustomCommandCodeType
import org.jetbrains.exposed.dao.id.LongIdTable

object CustomGuildCommands : LongIdTable() {
	val guild = reference("guild", ServerConfigs).index()
	val label = text("label")
	val enabled = bool("enabled")
	val codeType = enumeration("code_type", CustomCommandCodeType::class)
	val code = text("code")
}