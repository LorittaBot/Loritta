package net.perfectdreams.loritta.cinnamon.platform.utils.correios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosResponse
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosUnknownObjeto
import org.junit.jupiter.api.Test

class CorreiosEntitiesTest {
    @Test
    fun `test unknown package deserialization`() {
        val r = Json.decodeFromStream<CorreiosResponse>(CorreiosEntitiesTest::class.java.getResourceAsStream("/correios/unknown_object.json"))

        require(r.objeto.size == 1) { "Objects in the response must be 1!" }
        require(r.objeto.first() is CorreiosUnknownObjeto) { "Object in the is not an unknown object!" }
    }
}