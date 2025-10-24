package net.perfectdreams.bliss

import kotlinx.serialization.json.JsonElement
import web.dom.Element

data class BlissProcessRequestJsonBody(
    val element: Element?,
    val map: MutableMap<String, JsonElement>,
    var includeBody: Boolean
)