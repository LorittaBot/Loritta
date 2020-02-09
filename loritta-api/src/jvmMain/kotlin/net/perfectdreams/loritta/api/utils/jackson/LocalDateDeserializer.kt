package net.perfectdreams.loritta.utils.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException
import java.time.LocalDate

class LocalDateDeserializer protected constructor() : StdDeserializer<LocalDate>(LocalDate::class.java) {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LocalDate {
        return LocalDate.parse(jp.readValueAs(String::class.java))
    }

    companion object {
        private val serialVersionUID = 1L
    }
}