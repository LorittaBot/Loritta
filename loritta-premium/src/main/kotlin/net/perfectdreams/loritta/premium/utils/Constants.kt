package net.perfectdreams.loritta.premium.utils

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory
import net.perfectdreams.loritta.utils.jackson.FixedMapDeserializer

object Constants {
	val HOCON_MAPPER = ObjectMapper(HoconFactory()).apply {
		this.enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING)
		this.registerModule(ParameterNamesModule())
		val module = SimpleModule()

		// Workaround for https://github.com/jclawson/jackson-dataformat-hocon/issues/15
		module.setDeserializerModifier(object: BeanDeserializerModifier() {
			override fun modifyMapDeserializer(config: DeserializationConfig, type: MapType, beanDesc: BeanDescription, deserializer: JsonDeserializer<*>): JsonDeserializer<*> {
				return FixedMapDeserializer(type)
			}
		})

		this.registerModule(module)

		this.propertyNamingStrategy = object: PropertyNamingStrategy.PropertyNamingStrategyBase() {
			override fun translate(p0: String): String {
				val newField = StringBuilder()

				for (ch in p0) {
					if (ch.isUpperCase()) {
						newField.append('-')
					}
					newField.append(ch.toLowerCase())
				}

				return newField.toString()
			}
		}
	}

	const val PREMIUM_BOT_VALUE = 19.99
}