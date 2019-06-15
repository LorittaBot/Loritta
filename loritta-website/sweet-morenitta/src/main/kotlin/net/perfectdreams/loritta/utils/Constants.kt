package net.perfectdreams.loritta.utils

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory
import org.yaml.snakeyaml.Yaml

object Constants {
    const val DEFAULT_LOCALE_ID = "default"
    val YAML = Yaml()
    val JSON_MAPPER = ObjectMapper()
    val HOCON_MAPPER = ObjectMapper(HoconFactory()).apply {
        this.enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING)
        this.registerModule(ParameterNamesModule())

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
    const val DISCORD_CRAWLER_USER_AGENT = "Mozilla/5.0 (compatible; Discordbot/2.0; +https://discordapp.com)"
}