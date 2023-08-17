package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
sealed class BackgroundVariation {
    abstract val file: String
    abstract val preferredMediaType: String
    abstract val crop: Rectangle?
    abstract val storageType: BackgroundStorageType
}

@Serializable
class DefaultBackgroundVariation(
    override val file: String,
    override val preferredMediaType: String,
    override val crop: Rectangle?,
    override val storageType: BackgroundStorageType
) : BackgroundVariation() {
}

@Serializable
class ProfileDesignGroupBackgroundVariation(
    // TODO: This is actually a UUID, should be handled as a UUID (However there isn't mpp UUID yet)
    val profileDesignGroupId: String,
    override val file: String,
    override val preferredMediaType: String,
    override val crop: Rectangle?,
    override val storageType: BackgroundStorageType
) : BackgroundVariation()