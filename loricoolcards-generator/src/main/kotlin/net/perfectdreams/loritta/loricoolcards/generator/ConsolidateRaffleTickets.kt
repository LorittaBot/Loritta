package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import java.io.File

/**
 * Migrates RaffleTickets rows into a new clean table, keeping only:
 * - Winner ticket rows (referenced by Raffles.winnerTicket FK)
 * - All tickets from raffles with ID > 118488 (active/recent raffles)
 *
 * This is much faster than deleting millions of rows from the original table.
 * The script only creates and populates the new table — it does NOT drop/rename anything.
 * The actual table swap should be done manually during a maintenance window.
 */
private const val MAX_RAFFLE_ID_TO_CLEAN = 118488L
private const val NEW_TABLE_NAME = "raffletickets_new"

suspend fun main() {
    val configurationFile = File(System.getProperty("conf") ?: "./loricoolcards-production-stickers-generator.conf")

    if (!configurationFile.exists()) {
        println("Missing configuration file!")
        System.exit(1)
        return
    }

    val config = readConfigurationFromFile<LoriCoolCardsGeneratorProductionStickersConfig>(configurationFile)

    val pudding = Pudding.createPostgreSQLPudding(
        LorittaBot.SCHEMA_VERSION,
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )

    pudding.transaction {
        val jdbc = (this.connection as JdbcConnectionImpl).connection

        // Step 1: Create the new table with the same schema (columns, constraints, defaults)
        println("Creating $NEW_TABLE_NAME...")
        jdbc.createStatement().execute("CREATE TABLE IF NOT EXISTS $NEW_TABLE_NAME (LIKE ${RaffleTickets.tableName} INCLUDING DEFAULTS INCLUDING CONSTRAINTS)")
        println("Created $NEW_TABLE_NAME")

        // Step 2: Copy winner ticket rows from ended raffles (up to MAX_RAFFLE_ID_TO_CLEAN)
        println("Copying winner ticket rows...")
        val winnerTicketsCopied = jdbc.createStatement().executeUpdate("""
            INSERT INTO $NEW_TABLE_NAME
            SELECT rt.* FROM ${RaffleTickets.tableName} rt
            WHERE rt.id IN (
                SELECT winner_ticket FROM ${Raffles.tableName}
                WHERE winner_ticket IS NOT NULL
                  AND id <= $MAX_RAFFLE_ID_TO_CLEAN
            )
            ON CONFLICT DO NOTHING
        """.trimIndent())
        println("Copied $winnerTicketsCopied winner ticket rows")

        // Step 3: Copy ALL ticket rows from raffles after the cutoff (active/recent raffles we don't want to touch)
        println("Copying tickets from raffles with ID > $MAX_RAFFLE_ID_TO_CLEAN...")
        val recentTicketsCopied = jdbc.createStatement().executeUpdate("""
            INSERT INTO $NEW_TABLE_NAME
            SELECT rt.* FROM ${RaffleTickets.tableName} rt
            WHERE rt.raffle > $MAX_RAFFLE_ID_TO_CLEAN
            ON CONFLICT DO NOTHING
        """.trimIndent())
        println("Copied $recentTicketsCopied recent raffle ticket rows")

        println()
        println("Migration complete! Total rows in new table: ${winnerTicketsCopied + recentTicketsCopied}")
        println()
        println("To finish the swap during a maintenance window, run:")
        println("  1. ALTER TABLE ${RaffleTickets.tableName} RENAME TO raffletickets_old;")
        println("  2. ALTER TABLE $NEW_TABLE_NAME RENAME TO ${RaffleTickets.tableName};")
        println("  3. CREATE INDEX ON ${RaffleTickets.tableName} (\"user\");")
        println("  4. CREATE INDEX ON ${RaffleTickets.tableName} (raffle);")
        println("  5. SELECT setval('${RaffleTickets.tableName}_id_seq', (SELECT COALESCE(MAX(id), 0) FROM ${RaffleTickets.tableName}));")
        println("  6. After verifying everything works: DROP TABLE raffletickets_old;")
    }

    println("Done!")
}
