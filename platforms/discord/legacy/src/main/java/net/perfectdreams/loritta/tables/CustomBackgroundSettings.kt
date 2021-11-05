package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.tables.UserSettings
import org.jetbrains.exposed.dao.id.LongIdTable

object CustomBackgroundSettings : LongIdTable() {
    val settings = reference("settings", UserSettings).uniqueIndex() // We only want one reference to a settings entry here, that's why it is unique
    val path = text("path")
}