package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import java.sql.ResultSet

object DiscordGatewayEvents : LongIdTable() {
    val type = text("type").index()
    val payload = jsonb("payload")
}

class JsonBinary : ColumnType() {
    override fun sqlType() = "JSONB"

    override fun readObject(rs: ResultSet, index: Int): Any? {
        return rs.getString(index)
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = value as String?
        stmt[index] = obj
    }
}

fun Table.jsonb(name: String): Column<String> = registerColumn(name, JsonBinary())