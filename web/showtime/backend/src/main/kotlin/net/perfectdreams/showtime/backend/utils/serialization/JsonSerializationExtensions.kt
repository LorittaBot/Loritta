package net.perfectdreams.showtime.backend.utils.serialization

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

val JsonElement.jsonObjectArray
    get() = this.jsonArray.map { it.jsonObject }