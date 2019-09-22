package com.mrpowergamerbr.loritta.utils.exposed

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject
import java.sql.PreparedStatement

fun Table.rawJsonb(name: String, gson: Gson, jsonParser: JsonParser): Column<JsonElement>
		= registerColumn(name, RawJson(gson, jsonParser))

class RawJson(private val gson: Gson, private val jsonParser: JsonParser) : ColumnType() {
	override fun sqlType() = "jsonb"

	override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
		val obj = PGobject()
		obj.type = "jsonb"
		obj.value = value as String
		stmt.setObject(index, obj)
	}

	override fun valueFromDB(value: Any): Any {
		if (value !is PGobject)
			return value

		return try {
			jsonParser.parse(value.value)
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