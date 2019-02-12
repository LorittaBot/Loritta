package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.youtube.YouTubeWebhook
import java.io.File

class MigrationTool(val config: LorittaConfig) {
    fun migrateYouTubeWebhooks() {
        val loritta = Loritta(config)
        loritta.initMongo()
        loritta.initPostgreSql()

        println("Migrando YouTube Webhooks...")

        val youtubeWebhookFile = File(Loritta.FOLDER, "youtube_webhook.json")
        val youtubeWebhooks = gson.fromJson<List<OldYouTubeWebhook>>(youtubeWebhookFile.readText())

        val newYouTubeWebhooks = mutableMapOf<String, YouTubeWebhook>()

        youtubeWebhooks.forEach {
            newYouTubeWebhooks[it.channelId] = YouTubeWebhook(it.createdAt, it.lease)
        }

        youtubeWebhookFile.renameTo(File(Loritta.FOLDER, "youtube_webhook.pre_migration"))
        youtubeWebhookFile.writeText(gson.toJson(newYouTubeWebhooks))

        println("Webhooks migradas com sucesso!")
    }

    class OldYouTubeWebhook(val channelId: String, val createdAt: Long, val lease: Int)
}