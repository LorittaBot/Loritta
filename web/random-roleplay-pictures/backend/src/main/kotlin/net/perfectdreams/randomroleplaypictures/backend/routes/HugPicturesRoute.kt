package net.perfectdreams.randomroleplaypictures.backend.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.randomroleplaypictures.backend.utils.RoleplayPictures
import net.perfectdreams.sequins.ktor.BaseRoute

class HugPicturesRoute : BaseRoute("/api/v1/pictures/hug") {
    override suspend fun onRequest(call: ApplicationCall) {
        val gender1 = Gender.valueOf(call.parameters.getOrFail("gender1"))
        val gender2 = Gender.valueOf(call.parameters.getOrFail("gender2"))

        // Time to filter the pictures!
        val filteredPictures = RoleplayPictures.hugPictures.pictures
            .filter {
                // TODO: Decrease the chance of selecting a "generic" picture if both users have their genders set
                (it.matchType is RoleplayPictures.GenderMatchType && (it.matchType.gender1 == gender1 || it.matchType.gender2 == gender2)) || (it.matchType is RoleplayPictures.GenericMatchType)
            }

        // If the filteredPictures list is empty, select the full list
        val pictures = filteredPictures.ifEmpty { RoleplayPictures.hugPictures.pictures }

        val picture = pictures.random()
        call.respondText(
            Json.encodeToString(Picture(picture.path)),
            ContentType.Application.Json
        )
    }

    @Serializable
    data class Picture(
        val path: String
    )
}