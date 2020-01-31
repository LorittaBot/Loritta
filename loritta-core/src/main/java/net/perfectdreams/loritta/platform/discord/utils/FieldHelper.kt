package net.perfectdreams.loritta.platform.discord.utils

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.lang.reflect.Field
import java.lang.reflect.Modifier

object FieldHelper {
	private var MODIFIERS: VarHandle? = null

	fun makeNonFinal(field: Field) {
		val mods = field.modifiers
		if (Modifier.isFinal(mods)) {
			MODIFIERS!!.set(field, mods and Modifier.FINAL.inv())
		}
	}

	init {
		MODIFIERS = try {
			val lookup = MethodHandles.privateLookupIn(Field::class.java, MethodHandles.lookup())
			lookup.findVarHandle(Field::class.java, "modifiers", Int::class.javaPrimitiveType)
		} catch (ex: IllegalAccessException) {
			throw RuntimeException(ex)
		} catch (ex: NoSuchFieldException) {
			throw RuntimeException(ex)
		}
	}
}