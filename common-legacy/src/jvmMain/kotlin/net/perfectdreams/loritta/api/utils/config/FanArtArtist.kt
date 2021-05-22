package net.perfectdreams.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class FanArtArtist @JsonCreator constructor(
    @param:JsonProperty("id")
    @field:JsonProperty("id")
    val id: String,
		@param:JsonProperty("info")
    @field:JsonProperty("info")
    val info: Info,
		@param:JsonProperty("fanArts")
    @field:JsonProperty("fanArts")
    val fanArts: List<FanArt>,
		@param:JsonProperty("networks")
    @field:JsonProperty("networks")
    val socialNetworks: List<SocialNetwork>? = listOf()
) {
    data class Info(
        @param:JsonProperty("name")
        @field:JsonProperty("name")
        val name: String?,
        @param:JsonProperty("avatarUrl")
        @field:JsonProperty("avatarUrl")
        val avatarUrl: String?,
        @param:JsonProperty("override")
        @field:JsonProperty("override")
        val override: Info?
    )

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = SocialNetwork.GenericSocialNetwork::class
    )
    @JsonSubTypes(
        value = [
            JsonSubTypes.Type(
                value = SocialNetwork.DiscordSocialNetwork::class,
                name = "discord"
            ),
            JsonSubTypes.Type(
                value = SocialNetwork.TwitterSocialNetwork::class,
                name = "twitter"
            )
        ]
    )
    open class SocialNetwork(
        @JsonProperty("type")
        val type: String
    ) {
        class GenericSocialNetwork @JsonCreator constructor(
            @JsonProperty("name")
            val name: String?,
            @JsonProperty("display")
            val display: String?,
            @JsonProperty("link")
            val link: String?
        ) : SocialNetwork("generic")

        class DiscordSocialNetwork @JsonCreator constructor(
            @JsonProperty("id")
            val id: String
        ) : SocialNetwork("discord")

        class TwitterSocialNetwork(
            @JsonProperty("handle")
            val handle: String
        ) : SocialNetwork("twitter")
    }
}