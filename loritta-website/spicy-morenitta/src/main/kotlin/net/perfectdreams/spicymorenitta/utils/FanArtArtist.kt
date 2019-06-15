package net.perfectdreams.spicymorenitta.utils

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class FanArtArtist @JsonCreator constructor(
        @param:JsonProperty("id")
        @field:JsonProperty("id")
        val id: String,
        @param:JsonProperty("info")
        @field:JsonProperty("info")
        val info: Info,
        @Optional val user: User? = null,
        @param:JsonProperty("fanArts")
        @field:JsonProperty("fanArts")
        val fanArts: List<FanArt>
) {
    @Serializable
    data class Info(
            @param:JsonProperty("name")
            @field:JsonProperty("name")
            val name: String?,
            @param:JsonProperty("avatarUrl")
            @field:JsonProperty("avatarUrl")
            val avatarUrl: String?,
            @param:JsonProperty("override")
            @field:JsonProperty("override")
            @Optional val override: Info? = null
    )

    @Serializable
    data class User(
            val name: String,
            val effectiveAvatarUrl: String
    )
}