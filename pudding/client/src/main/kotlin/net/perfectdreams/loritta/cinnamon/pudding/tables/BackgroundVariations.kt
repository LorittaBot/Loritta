package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object BackgroundVariations : LongIdTable() {
    val background = reference("background", Backgrounds)
    val profileDesignGroup = optReference("profile_design_group", ProfileDesignGroups)
    val file = text("file")
    val preferredMediaType = text("preferred_media_type")
    val crop = jsonb("crop").nullable()

    init {
        // Combined index, we can only have a crop for a specific background + design group, not multiple
        // This however does NOT WORK with upsert because profileDesignGroup can be null, and that breaks things
        // So manual checks must be made instead of relying on upsert!
        uniqueIndex(background, profileDesignGroup)
    }
}