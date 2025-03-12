package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.serializable.BackgroundStorageType
import org.jetbrains.exposed.dao.id.LongIdTable

object BackgroundVariations : LongIdTable() {
    val background = reference("background", Backgrounds).index()
    val profileDesignGroup = optReference("profile_design_group", ProfileDesignGroups)
    val file = text("file")
    val preferredMediaType = text("preferred_media_type")
    val crop = jsonb("crop").nullable()
    val storageType = postgresEnumeration<BackgroundStorageType>("storage_type").default(BackgroundStorageType.DREAM_STORAGE_SERVICE)

    init {
        // Combined index, we can only have a crop for a specific background + design group, not multiple
        // This however does NOT WORK with upsert because profileDesignGroup can be null, and that breaks things
        // So manual checks must be made instead of relying on upsert!
        uniqueIndex(background, profileDesignGroup)
    }
}