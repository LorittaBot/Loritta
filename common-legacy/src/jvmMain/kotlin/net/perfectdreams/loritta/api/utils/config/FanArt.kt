package net.perfectdreams.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import net.perfectdreams.loritta.utils.jackson.LocalDateDeserializer
import net.perfectdreams.loritta.utils.jackson.LocalDateSerializer
import java.time.LocalDate

data class FanArt(
    @param:JsonProperty("fileName")
    @field:JsonProperty("fileName")
    val fileName: String,
    @param:JsonProperty("createdAt")
    @field:JsonProperty("createdAt")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val createdAt: LocalDate,
    @param:JsonProperty("tags")
    @field:JsonProperty("tags")
    val tags: Set<String>
)