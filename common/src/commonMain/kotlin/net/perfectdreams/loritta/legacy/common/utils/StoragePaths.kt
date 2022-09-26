package net.perfectdreams.loritta.legacy.common.utils

object StoragePaths {
    fun Background(file: String) = StoragePath("profiles/backgrounds", file)

    fun CustomBackground(userId: Long, file: String) = StoragePath("profiles/backgrounds/custom/${userId}", file)

    fun CustomBadge(serverId: Long, file: String) = StoragePath("badges/custom/$serverId", file)

    data class StoragePath(
        val folder: String,
        val file: String
    ) {
        fun join() = "$folder/$file"
    }
}