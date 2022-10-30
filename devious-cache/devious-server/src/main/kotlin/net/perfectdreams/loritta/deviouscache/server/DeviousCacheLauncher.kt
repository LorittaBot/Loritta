package net.perfectdreams.loritta.deviouscache.server

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.deviouscache.server.utils.config.BaseConfig
import org.jetbrains.exposed.sql.Database
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val config = Hocon.decodeFromConfig<BaseConfig>(ConfigFactory.parseFile(File("./devious-cache.conf")))

    // Improve throughput
    // https://phiresky.github.io/blog/2020/sqlite-performance-tuning/
    // journal_mode = WAL improves performance
    // synchronous = We don't care about data loss
    // temp_store = We don't want to store indexes in memory, we already have the data stored in memory
    val database = Database.connect("jdbc:sqlite:cache/devious.db?journal_mode=wal&synchronous=off&mmap_size=30000000000&temp_store=0&page_size=32768")

    val m = DeviousCache(config, database)
    m.start()
}