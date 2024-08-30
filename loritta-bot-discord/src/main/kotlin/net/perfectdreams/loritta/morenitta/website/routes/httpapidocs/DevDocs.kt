package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DevDocs(
    val sidebar: List<SidebarCategory>
) {
    @Serializable
    data class SidebarCategory(
        val name: String?,
        val pages: List<SidebarPageEntry>
    ) {
        @Serializable
        sealed class SidebarPageEntry

        @Serializable
        @SerialName("text")
        data class SidebarDocsPageEntry(
            val file: String,
            val icon: String
        ) : SidebarPageEntry()

        @Serializable
        @SerialName("endpoint")
        data class SidebarEndpointPageEntry(
            val endpointId: String
        ) : SidebarPageEntry()
    }
}