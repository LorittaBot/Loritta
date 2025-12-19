package net.perfectdreams.dora

data class Project(
    val id: Long,
    val slug: String,
    val name: String,
    val iconUrl: String? = null
)