package net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables

import org.jetbrains.exposed.dao.LongIdTable

object ScreenshotProofs : LongIdTable() {
	val report = reference("report", UserReports).index()

	val url = text("url")
}