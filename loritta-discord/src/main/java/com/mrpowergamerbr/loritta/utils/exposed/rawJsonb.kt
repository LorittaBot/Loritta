package com.mrpowergamerbr.loritta.utils.exposed

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

fun Table.rawJsonb(name: String, gson: Gson): Column<JsonElement>
		= registerColumn(name, RawJson(gson))

class RawJson(private val gson: Gson) : ColumnType() {
	override fun sqlType() = "jsonb"

	override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
		val obj = PGobject()
		obj.type = "jsonb"
		obj.value = value as String?
		stmt[index] = obj
	}

	override fun valueFromDB(value: Any): Any {
		if (value !is PGobject)
			return value

		return try {
			JsonParser.parseString(value.value)
		} catch (e: Exception) {
			e.printStackTrace()
			throw RuntimeException("Can't parse JSON: $value")
		}
	}

	override fun notNullValueToDB(value: Any): Any {
		if (value is String)
			return value
		return gson.toJson(value)
	}

	override fun nonNullValueToString(value: Any): String = "'${gson.toJson(value)}'"
}