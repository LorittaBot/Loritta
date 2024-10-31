package net.perfectdreams.loritta.cinnamon.pudding

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.createOrUpdatePostgreSQLEnum
import net.perfectdreams.loritta.cinnamon.pudding.services.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.bomdiaecia.BomDiaECiaMatchLosers
import net.perfectdreams.loritta.cinnamon.pudding.tables.bomdiaecia.BomDiaECiaMatchWinners
import net.perfectdreams.loritta.cinnamon.pudding.tables.bomdiaecia.BomDiaECiaMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Drops
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.CollectedChristmas2022Points
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.CollectedEaster2023Eggs
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.CreatedEaster2023Baskets
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.Easter2023Drops
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.Easter2023Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.CorreiosPackageUpdateUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxTaxedUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxWarnUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.UserAskedRaffleNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.stats.LorittaClusterStats
import net.perfectdreams.loritta.cinnamon.pudding.tables.stats.LorittaDiscordShardStats
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.PuddingTasks
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.common.commands.InteractionContextType
import net.perfectdreams.loritta.common.components.ComponentType
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentGenre
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentType
import net.perfectdreams.loritta.common.utils.*
import net.perfectdreams.loritta.common.utils.easter2023.EasterEggColor
import net.perfectdreams.loritta.serializable.BackgroundStorageType
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class Pudding(
    val hikariDataSource: HikariDataSource,
    val database: Database,
    private val cachedThreadPool: ExecutorService,
    val dispatcher: CoroutineDispatcher,
    permits: Int
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
        private val ISOLATION_LEVEL =
            IsolationLevel.TRANSACTION_REPEATABLE_READ // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!
        private const val SCHEMA_VERSION = 70 // Bump this every time any table is added/updated!
        private val SCHEMA_ID = UUID.fromString("600556aa-2920-41c7-b26c-7717eff2d392") // This is a random unique ID, it is used for upserting the schema version

        /**
         * Creates a Pudding instance backed by a PostgreSQL database
         *
         * @param address      the PostgreSQL address
         * @param databaseName the database name in PostgreSQL
         * @param username     the PostgreSQL username
         * @param password     the PostgreSQL password
         * @return a [Pudding] instance backed by a PostgreSQL database
         */
        fun createPostgreSQLPudding(
            address: String,
            databaseName: String,
            username: String,
            password: String,
            permits: Int = 128,
            builder: HikariConfig.() -> (Unit) = {}
        ): Pudding {
            val hikariConfig = createHikariConfig(builder)
            hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName?ApplicationName=${"Loritta Cinnamon Pudding - " + HostnameUtils.getHostname()}"

            hikariConfig.username = username
            hikariConfig.password = password

            val hikariDataSource = HikariDataSource(hikariConfig)

            val cachedThreadPool = Executors.newCachedThreadPool()

            return Pudding(
                hikariDataSource,
                connectToDatabase(hikariDataSource),
                cachedThreadPool,
                // Instead of using Dispatchers.IO directly, we will create a cached thread pool.
                // This avoids issues when all Dispatchers.IO threads are blocked on transactions, causing any other coroutine using the Dispatcher.IO job to be
                // blocked.
                // Example: 64 blocked coroutines due to transactions (64 = max threads in a Dispatchers.IO dispatcher) + you also have a WebSocket listening for events, when the WS tries to
                // read incoming events, it is blocked because there isn't any available Dispatchers.IO threads!
                cachedThreadPool.asCoroutineDispatcher(),
                permits
            )
        }

        private fun createHikariConfig(builder: HikariConfig.() -> (Unit)): HikariConfig {
            val hikariConfig = HikariConfig()

            hikariConfig.driverClassName = DRIVER_CLASS_NAME

            // https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
            hikariConfig.addDataSourceProperty("reWriteBatchedInserts", "true")

            // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
            // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
            // https://stackoverflow.com/a/41206003/7271796
            hikariConfig.isAutoCommit = false

            // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
            hikariConfig.leakDetectionThreshold = 30L * 1000
            hikariConfig.transactionIsolation = ISOLATION_LEVEL.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

            hikariConfig.maximumPoolSize = 16
            hikariConfig.poolName = "PuddingPool"

            hikariConfig.apply(builder)

            return hikariConfig
        }

        // Loritta (Legacy) uses this!
        @OptIn(ExperimentalKeywordApi::class)
        fun connectToDatabase(dataSource: HikariDataSource): Database =
            Database.connect(
                dataSource,
                databaseConfig = DatabaseConfig {
                    defaultRepetitionAttempts = 5
                    defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
                    // "A table that is created with a keyword identifier now logs a warning that the identifier's case may be lost when it is automatically quoted in generated SQL."
                    // "DatabaseConfig now includes the property preserveKeywordCasing, which can be set to true to remove these warnings and to ensure that the identifier matches the exact case used."
                    // Our tables are all in lowercase because that's what the default Exposed behavior was, so we need to set this to false
                    preserveKeywordCasing = false
                }
            )
    }

    val users = UsersService(this)
    val serverConfigs = ServerConfigsService(this)
    val sonhos = SonhosService(this)
    val shipEffects = ShipEffectsService(this)
    val marriages = MarriagesService(this)
    val interactionsData = InteractionsDataService(this)
    val executedInteractionsLog = ExecutedInteractionsLogService(this)
    val backgrounds = BackgroundsService(this)
    val profileDesigns = ProfileDesignsService(this)
    val bovespaBroker = BovespaBrokerService(this)
    val bets = BetsService(this)
    val payments = PaymentsService(this)
    val stats = StatsService(this)
    val puddingTasks = PuddingTasks(this)
    val patchNotesNotifications = PatchNotesNotificationsService(this)
    val packagesTracking = PackagesTrackingService(this)
    val notifications = NotificationsService(this)
    val reputations = ReputationsService(this)

    // Used to avoid having a lot of threads being created on the "dispatcher" just to be blocked waiting for a connection, causing thread starvation and an OOM kill
    val semaphore = Semaphore(permits)
    val random = SecureRandom()

    /**
     * Starts tasks related to [Pudding], like table partition creation, purge old data, etc.
     *
     * If you are using Pudding just to interact with tables, and you don't care about the tasks, then you don't need to start the tasks!
     */
    fun startPuddingTasks() = puddingTasks.start()

    /**
     * Creates missing tables and columns in the database.
     *
     * To avoid updating tables that are being used by legacy applications (Loritta Legacy), the [shouldBeUpdated] parameter
     * can be used to filter what tables should be created.
     *
     * @param shouldBeUpdated checks if a table should be created/updated or not, if true, it will be included to be updated, if false, it won't
     */
    suspend fun createMissingTablesAndColumns(shouldBeUpdated: (String) -> Boolean) {
        val schemas = mutableListOf<Table>(SchemaVersion) // The schema version should always be created
        fun insertIfValid(vararg tables: Table) =
            schemas.addAll(tables.filter { shouldBeUpdated.invoke(it::class.simpleName!!) })
        insertIfValid(
            AuditLog,
            BackgroundPayments,
            Backgrounds,
            BackgroundVariations,
            BannedIps,
            BannedUsers,
            Birthday2020Drops,
            Birthday2020Players,
            BlacklistedGuilds,
            BomDiaECiaMatches,
            BomDiaECiaMatchLosers,
            BomDiaECiaMatchWinners,
            BomDiaECiaWinners,
            BoostedCandyChannels,
            BotVotes,
            BotVotesUserAvailableNotifications,
            BoughtStocks,
            BrowserFingerprints,
            CachedDiscordUsers,
            CachedDiscordUsersDirectMessageChannels,
            CachedGoogleVisionOCRResults,
            CachedYouTubeChannelIds,
            Christmas2022Drops,
            Christmas2022Players,
            CollectedChristmas2022Points,
            CoinFlipBetGlobalMatchmakingQueue,
            CoinFlipBetGlobalMatchmakingResults,
            CoinFlipBetGlobalSonhosTransactionsLog,
            CoinFlipBetMatchmakingResults,
            CollectedBirthday2020Points,
            CollectedCandies,
            CollectedChristmas2019Points,
            ConcurrentLoginBuckets,
            CustomBackgroundSettings,
            Dailies,
            DailyProfileShopItems,
            DailyShopItems,
            DailyShops,
            DiscordLorittaApplicationCommandHashes,
            DonationKeys,
            CollectedEaster2023Eggs,
            CreatedEaster2023Baskets,
            Easter2023Drops,
            Easter2023Players,
            EconomyState,
            EmojiFightMatches,
            EmojiFightMatchmakingResults,
            EmojiFightParticipants,
            ExecutedCommandsLog,
            ExecutedComponentsLog,
            GatewayActivities,
            GuildCountStats,
            GuildProfiles,
            Halloween2019Players,
            InteractionsData,
            // LoriTuberChannels,
            // LoriTuberCharacters,
            // LoriTuberMails,
            // LoriTuberPendingVideos,
            // LoriTuberServerInfos,
            Marriages,
            MiscellaneousData,
            Mutes,
            CorreiosPackageUpdateUserNotifications,
            DailyTaxTaxedUserNotifications,
            DailyTaxWarnUserNotifications,
            UserNotifications,
            PatchNotesNotifications,
            Payments,
            PaymentSonhosTransactionResults,
            PendingImportantNotifications,
            ProfileDesignGroupEntries,
            ProfileDesignGroups,
            ProfileDesigns,
            ProfileDesignsPayments,
            Profiles,
            Raffles,
            RaffleTickets,
            UserAskedRaffleNotifications,
            Raspadinhas,
            ReceivedPatchNotesNotifications,
            Reminders,
            Reputations,
            SchemaVersion,
            SentYouTubeVideoIds,
            CustomGuildCommands,
            GiveawayParticipants,
            Giveaways,
            GuildProfiles,
            AutoroleConfigs,
            DonationConfigs,
            EconomyConfigs,
            EventLogConfigs,
            ExperienceRoleRates,
            InviteBlockerConfigs,
            LevelAnnouncementConfigs,
            LevelConfigs,
            MemberCounterChannelConfigs,
            MiscellaneousConfigs,
            ModerationConfigs,
            ModerationPredefinedPunishmentMessages,
            ModerationPunishmentMessagesConfig,
            ReactionOptions,
            RolesByExperience,
            StarboardConfigs,
            TrackedTwitchAccounts,
            AuthorizedTwitchAccounts,
            AlwaysTrackTwitchAccounts,
            PremiumTrackTwitchAccounts,
            TwitchEventSubEvents,
            CachedTwitchChannels,
            TrackedTwitterAccounts,
            TrackedYouTubeAccounts,
            WarnActions,
            WelcomerConfigs,
            ServerConfigs,
            ServerRolePermissions,
            Sets,
            ShipEffects,
            SonhosBundles,
            SonhosTransaction,
            SonhosTransactionsLog,
            SpicyStacktraces,
            Sponsors,
            StarboardMessages,
            StoredMessages,
            TickerPrices,
            TrackedCorreiosPackages,
            TrackedCorreiosPackagesEvents,
            BotVoteSonhosTransactionsLog,
            BrokerSonhosTransactionsLog,
            Christmas2022SonhosTransactionsLog,
            CoinFlipBetSonhosTransactionsLog,
            DailyRewardSonhosTransactionsLog,
            DailyTaxSonhosTransactionsLog,
            DivineInterventionSonhosTransactionsLog,
            Easter2023SonhosTransactionsLog,
            EmojiFightSonhosTransactionsLog,
            ExecutedApplicationCommandsLog,
            PaymentSonhosTransactionsLog,
            PowerStreamClaimedFirstSonhosRewardSonhosTransactionsLog,
            PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionsLog,
            RaffleRewardSonhosTransactionsLog,
            RaffleTicketsSonhosTransactionsLog,
            ShipEffectSonhosTransactionsLog,
            SonhosBundlePurchaseSonhosTransactionsLog,
            SparklyPowerLSXSonhosTransactionsLog,
            UserAchievements,
            UserSettings,
            UsersFollowingCorreiosPackages,
            YouTubeEventSubEvents,
            DailyTaxNotifiedUsers,
            SimpleSonhosTransactionsLog,
            FanArtsExtravaganza,
            AprilFoolsCoinFlipBugs,
            SlashCommandsScopeAuthorizations,
            LoriCoolCardsEvents,
            LoriCoolCardsEventCards,
            LoriCoolCardsSeenCards,
            LoriCoolCardsUserOwnedCards,
            LoriCoolCardsFinishedAlbumUsers,
            LoriCoolCardsUserBoughtBoosterPacks,
            UserWebsiteSettings,
            UserFavoritedGuilds,
            UserPocketLorittaSettings,
            LoriCoolCardsUserTrades,
            UserLorittaAPITokens,
            TrackedBlueskyAccounts,
            GuildCommandConfigs,
            WebsiteDiscountCoupons,
            SonhosTransferRequests,
            TotalSonhosStats,
            DiscordLorittaApplicationEmojis,
            LorittaDiscordShardStats,
            LorittaClusterStats,
            ReactionEventPlayers,
            ReactionEventDrops,
            CollectedReactionEventPoints,
            CraftedReactionEventItems,
            ReactionEventsConfigs,
            ReactionEventFinishedEventUsers
        )

        if (schemas.isNotEmpty())
            transaction {
                // SchemaUtils is dangerous: If PostgreSQL is running a VACUUM, the app will wait until the lock is released, because Exposed
                // tries loading the table info data from the table, and that conflicts with VACUUM.
                // (Yes, Exposed will query the table info data EVEN IF there isn't updates to be done, smh)
                //
                // To work around this, we will store the current schema version on the database and ONLY THEN update the data.
                // This also allows us to implement data migration steps down the road, yay!

                // We will first lock to avoid multiple processes trying to update the data at the same time
                val xactLockStatement = (this.connection as JdbcConnectionImpl).connection.prepareStatement("SELECT pg_advisory_xact_lock(?);")
                xactLockStatement.setInt(1, "loritta-cinnamon-pudding-schema-updater".hashCode())
                xactLockStatement.execute()

                if (checkIfTableExists(SchemaVersion)) {
                    val schemaVersion =
                        SchemaVersion.slice(SchemaVersion.version).select { SchemaVersion.id eq SCHEMA_ID }
                            .firstOrNull()
                            ?.get(SchemaVersion.version)

                    if (!java.lang.Boolean.getBoolean("loritta.ignoreSchemaVersion")) {
                        if (schemaVersion == SCHEMA_VERSION) {
                            logger.info { "Database schema version matches (database: ${schemaVersion}; schema: $SCHEMA_VERSION), so we won't update any tables, yay!" }
                            return@transaction
                        } else {
                            if (schemaVersion != null && schemaVersion > SCHEMA_VERSION) {
                                logger.warn { "Database schema version is newer (database: ${schemaVersion}; schema: $SCHEMA_VERSION), so we will not update the tables to avoid issues..." }
                                return@transaction
                            } else {
                                logger.info { "Database schema version is older (database: ${schemaVersion}; schema: $SCHEMA_VERSION), so we will update the tables, yay!" }
                            }
                        }
                    } else {
                        logger.info { "Ignoring schema version (database: ${schemaVersion}; schema: $SCHEMA_VERSION), so we will update the tables anyway, yay!" }
                    }
                } else {
                    logger.warn { "SchemaVersion doesn't seem to exist, we will ignore the schema version check..." }
                }

                createOrUpdatePostgreSQLEnum(BackgroundStorageType.values())
                createOrUpdatePostgreSQLEnum(AchievementType.values())
                createOrUpdatePostgreSQLEnum(ApplicationCommandType.values())
                createOrUpdatePostgreSQLEnum(ComponentType.values())
                createOrUpdatePostgreSQLEnum(LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.values())
                createOrUpdatePostgreSQLEnum(SparklyPowerLSXTransactionEntryAction.values())
                createOrUpdatePostgreSQLEnum(DivineInterventionTransactionEntryAction.values())
                createOrUpdatePostgreSQLEnum(WebsiteVoteSource.values())
                createOrUpdatePostgreSQLEnum(PendingImportantNotificationState.values())
                createOrUpdatePostgreSQLEnum(LoriTuberContentLength.values())
                createOrUpdatePostgreSQLEnum(LoriTuberContentType.values())
                createOrUpdatePostgreSQLEnum(LoriTuberContentGenre.values())
                createOrUpdatePostgreSQLEnum(EasterEggColor.values())
                createOrUpdatePostgreSQLEnum(RaffleType.values())
                createOrUpdatePostgreSQLEnum(TransactionType.values())
                createOrUpdatePostgreSQLEnum(CardRarity.values())
                createOrUpdatePostgreSQLEnum(ColorTheme.values())
                createOrUpdatePostgreSQLEnum(InteractionContextType.values())

                logger.info { "Tables to be created or updated: $schemas" }
                SchemaUtils.createMissingTablesAndColumns(
                    *schemas
                        .toMutableList()
                        // Partitioned tables
                        .filter { it !in listOf(ExecutedApplicationCommandsLog, ExecutedComponentsLog) }
                        .toTypedArray()
                )

                // This is a workaround because Exposed does not support (yet) Partitioned Tables
                if (ExecutedApplicationCommandsLog in schemas)
                    createPartitionedTable(ExecutedApplicationCommandsLog)
                if (ExecutedComponentsLog in schemas)
                    createPartitionedTable(ExecutedComponentsLog)

                logger.info { "Updating database schema version to $SCHEMA_VERSION..." }

                SchemaVersion.upsert(SchemaVersion.id) {
                    it[SchemaVersion.id] = SCHEMA_ID
                    it[SchemaVersion.version] = SCHEMA_VERSION
                }
            }
    }

    private fun Transaction.createPartitionedTable(table: Table) {
        // The reason we use partitioned tables for the ExecutedApplicationCommandsLog table, is because there is a LOT of commands there
        // Removing old logs is painfully slow due to vacuuming and stuff, querying recent commands is also pretty slow.
        // So it is better to split stuff up in separate partitions!
        val createStatements = createStatementsPartitioned(table, "RANGE(sent_at)")

        execStatements(false, createStatements)
        commit()

        val alterStatements = SchemaUtils.addMissingColumnsStatements(
            table
        )

        // Now call the addMissingColumnsStatements again with the partitioned tables
        // We can not use createMissingTablesAndColumns here because Exposed will think that the table does not exist
        // because it is a partitioned table!
        execStatements(false, alterStatements)
        commit()
    }

    // From Exposed
    private fun Transaction.execStatements(inBatch: Boolean, statements: List<String>) {
        if (inBatch) {
            execInBatch(statements)
        } else {
            for (statement in statements) {
                exec(statement)
            }
        }
    }

    // This is a workaround because "Table.exists()" does not work for partitioned tables!
    private fun Transaction.checkIfTableExists(table: Table): Boolean {
        val tableScheme = table.tableName.substringBefore('.', "").takeIf { it.isNotEmpty() }
        val schema = tableScheme?.inProperCase() ?: TransactionManager.current().connection.metadata {
            // TODO: I'm not sure how to correctly get the schema names, before we used "this.currentScheme" but that's has since been removed
            // The result of "schemaNames" is [information_schema, pg_catalog, public]
            // We could hardcode the "public" result, but let's throw an error if it isn't found
            this.schemaNames.firstOrNull { it == "public" } ?: error("Missing \"public\" schema")
        }
        val tableName = TransactionManager.current().identity(table) // Yes, because "Table.tableName" does not return the correct name...

        return exec("SELECT EXISTS (\n" +
                "   SELECT FROM information_schema.tables \n" +
                "   WHERE  table_schema = '$schema'\n" +
                "   AND    table_name   = '$tableName'\n" +
                "   )") {
            it.next()
            it.getBoolean(1) // It should always be the first column, right?
        } ?: false
    }

    // From Exposed
    private fun String.inProperCase(): String =
        TransactionManager.currentOrNull()?.db?.identifierManager?.inProperCase(this@inProperCase) ?: this


    // From Exposed, this is the "createStatements" method but with a few changes
    private fun Transaction.createStatementsPartitioned(table: Table, partitionBySuffix: String): List<String> {
        if (checkIfTableExists(table))
            return emptyList()

        val alters = arrayListOf<String>()

        val (create, alter) = table.ddl.partition { it.startsWith("CREATE ") }

        val createTableSuffixed = create.map { "$it PARTITION BY $partitionBySuffix" }

        val indicesDDL = table.indices.flatMap { SchemaUtils.createIndex(it) }
        alters += alter

        return createTableSuffixed + indicesDDL + alter
    }

    suspend fun <T> transaction(repetitions: Int = 5, transactionIsolation: Int? = null, statement: suspend Transaction.() -> T) = suspendableNestableTransaction(
        dispatcher,
        database,
        repetitions,
        transactionIsolation,
        {
            semaphore.withPermit {
                it.invoke()
            }
        },
        statement
    )

    fun shutdown() {
        puddingTasks.shutdown()
        cachedThreadPool.shutdown()
    }

    /**
     * Setups a shutdown hook to shut down the [puddingTasks] when the application shutdowns.
     */
    fun setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                // Shutdown services when stopping the application
                // This is needed for the Pudding Tasks
                shutdown()
            }
        )
    }
}
