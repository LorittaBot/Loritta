package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object MiscellaneousConfigs : LongIdTable() {
    val enableBomDiaECia = bool("enable_bom_dia_e_cia").default(false)
    val enableQuirky = bool("enable_quirky").default(false)
}