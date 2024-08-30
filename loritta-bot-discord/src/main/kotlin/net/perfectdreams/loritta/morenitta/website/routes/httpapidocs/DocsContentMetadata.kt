package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DocsContentMetadata {
    abstract val title: String

    @SerialName("text")
    @Serializable
    class TextDocsContentMetadata(
        override val title: String
    ) : DocsContentMetadata()
}