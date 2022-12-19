package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.interactions.commands.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.utils.data.DataObject
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.CommandMentions
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordLorittaApplicationCommandHashes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.utils.GuildLorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import org.postgresql.util.PGobject
import java.util.*

class InteractionsListener(private val loritta: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    val manager = UnleashedCommandManager(loritta, loritta.languageManager)
    private var hasAlreadyGloballyUpdatedTheCommands = false

    override fun onReady(event: ReadyEvent) {
        // Update Slash Commands
        if (loritta.config.loritta.interactions.registerGlobally && !hasAlreadyGloballyUpdatedTheCommands) {
            hasAlreadyGloballyUpdatedTheCommands = true

            GlobalScope.launch {
                val registeredCommands = updateCommands(
                    0
                ) { commands ->
                    event.jda.updateCommands {
                        addCommands(*commands.toTypedArray())
                    }.complete()
                }

                loritta.commandMentions = CommandMentions(registeredCommands)
            }
        }

        if (!loritta.config.loritta.interactions.registerGlobally) {
            GlobalScope.launch {
                event.jda.guilds.filter { it.idLong in loritta.config.loritta.interactions.guildsToBeRegistered.map { it.toLong() } }
                    .forEach {
                        val registeredCommands = updateCommands(
                            it.idLong
                        ) { commands ->
                            it.updateCommands {
                                addCommands(*commands.toTypedArray())
                            }.complete()
                        }

                        loritta.commandMentions = CommandMentions(registeredCommands)
                    }
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        loritta.launchMessageJob(event) {
            var slashDeclaration: SlashCommandDeclaration? = null

            for (declaration in manager.slashCommands) {
                val rootLabel = event.name
                val subcommandGroupLabel = event.subcommandGroup
                val subcommandLabel = event.subcommandName

                if (rootLabel == manager.slashCommandDefaultI18nContext.get(declaration.name)) {
                    if (subcommandGroupLabel == null && subcommandLabel == null) {
                        // Already found it, yay!
                        slashDeclaration = declaration
                    } else {
                        // Check root subcommands
                        if (subcommandLabel != null) {
                            if (subcommandGroupLabel == null) {
                                // "/name subcommand"
                                slashDeclaration = declaration.subcommands.firstOrNull { manager.slashCommandDefaultI18nContext.get(it.name) == subcommandLabel }
                                break
                            } else {
                                // "/name subcommandGroup subcommand"
                                slashDeclaration = declaration.subcommandGroups.firstOrNull { manager.slashCommandDefaultI18nContext.get(it.name) == subcommandGroupLabel }
                                    ?.subcommands
                                    ?.firstOrNull {
                                        manager.slashCommandDefaultI18nContext.get(it.name) == subcommandLabel
                                    }
                                break
                            }
                        }
                    }
                    break
                }
            }

            // We should throw an error here
            // But we won't because we still use Discord InteraKTions
            if (slashDeclaration == null)
                return@launchMessageJob

            val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")

            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ApplicationCommandContext? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }

                val args = SlashCommandArguments(event)
                context = ApplicationCommandContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )

                executor.execute(
                    context,
                    args
                )
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()
            }
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        GlobalScope.launch {
            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ComponentContext? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }
                val callbackId = loritta.interactivityManager.buttonInteractionCallbacks[UUID.fromString(event.componentId)]
                context = ComponentContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )
                callbackId?.invoke(context)
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()
            }
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        GlobalScope.launch {
            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ComponentContext? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }
                val callbackId = loritta.interactivityManager.selectMenuInteractionCallbacks[UUID.fromString(event.componentId)]
                context = ComponentContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )
                callbackId?.invoke(context, event.interaction.values)
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()
            }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        GlobalScope.launch {
            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ModalContext? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }
                // val callbackId = loritta.componentManager.buttonInteractionCallbacks[UUID.fromString(event.componentId)]
                context = ModalContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )
                loritta.interactivityManager.modalCallback?.invoke(context, ModalArguments(event))
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()
            }
        }
    }

    private fun updateCommands(guildId: Long, action: (List<CommandData>) -> (List<Command>)): List<DiscordCommand> {
        val applicationCommands = manager.slashCommands.map { manager.convertDeclarationToJDA(it) } + loritta.interactionsManager.interaKTions.manager.applicationCommandsDeclarations.map { manager.convertInteraKTionsDeclarationToJDA(it) }
        val applicationCommandsHash = applicationCommands.sumOf { it.toData().toString().hashCode() }

        var registeredCommands: List<DiscordCommand>? = null

        loritta.pudding.hikariDataSource.connection.use { connection ->
            // First, we will hold a lock to avoid other instances trying to update the app commands at the same time
            val xactLockStatement = connection.prepareStatement("SELECT pg_advisory_xact_lock(?);")
            xactLockStatement.setInt(1, "loritta-cinnamon-application-command-updater".hashCode())
            xactLockStatement.execute()

            val pairData =
                connection.prepareStatement("SELECT hash, data FROM ${DiscordLorittaApplicationCommandHashes.tableName} WHERE id = $guildId;")
                    .executeQuery()
                    .let {
                        if (it.next())
                            Pair(it.getInt("hash"), it.getString("data"))
                        else
                            null
                    }

            if (pairData == null || applicationCommandsHash != pairData.first) {
                // Needs to be updated!
                logger.info { "Updating Loritta commands in guild $guildId... Hash: $applicationCommandsHash" }
                val updatedCommands = action.invoke(applicationCommands)
                val updatedCommandsData = updatedCommands.map {
                    DiscordCommand.from(it)
                }

                val updateStatement = connection.prepareStatement("INSERT INTO ${DiscordLorittaApplicationCommandHashes.tableName} (id, hash, data) VALUES ($guildId, $applicationCommandsHash, ?) ON CONFLICT (id) DO UPDATE SET hash = $applicationCommandsHash, data = ?;")

                val pgObject = PGobject()
                pgObject.type = "jsonb"
                pgObject.value = Json.encodeToString(updatedCommandsData)
                updateStatement.setObject(1, pgObject)
                updateStatement.setObject(2, pgObject)
                updateStatement.executeUpdate()

                logger.info { "Successfully updated Loritta's commands in guild $guildId! Hash: $applicationCommandsHash" }
                registeredCommands = updatedCommandsData
            } else {
                // No need for update, yay :3
                logger.info { "Stored guild $guildId commands hash match our hash $applicationCommandsHash, so we don't need to update, yay! :3" }
                registeredCommands = Json.decodeFromString(pairData.second)
            }

            connection.commit()
        }

        return registeredCommands!!
    }
}