package net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.gabrielaimageserver.data.MinecraftSkinLorittaSweatshirtRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.gabrielaimageserver.exceptions.InvalidMinecraftSkinException
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.URLUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import java.util.*

class MinecraftCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Minecraft
        private val I18N_CATEGORY_PREFIX = I18nKeysData.Commands.Category.Minecraft
        private val VALID_NAME_REGEX = Regex("[a-zA-Z0-9_]{2,16}")
    }

    val mojang = MinecraftMojangAPI()

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_CATEGORY_PREFIX.Name /* TODO: Use the category description */, CommandCategory.MINECRAFT, UUID.fromString("4b218716-6dbd-48c1-93fb-8f389c2f635e")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommandGroup(I18N_PREFIX.Player.Label, I18N_PREFIX.Player.Description) {
            subcommand(I18N_PREFIX.Player.Skin.Label, I18N_PREFIX.Player.Skin.Description, UUID.fromString("df20d1dc-26cc-4895-a17c-9fa9417d869a")) {
                executor = McSkinExecutor()
            }

            subcommand(I18N_PREFIX.Player.Avatar.Label, I18N_PREFIX.Player.Avatar.Description, UUID.fromString("fb5e21b5-fa3c-4abf-bf45-6a03e8b3f0c2")) {
                executor = McAvatarExecutor()
            }

            subcommand(I18N_PREFIX.Player.Head.Label, I18N_PREFIX.Player.Head.Description, UUID.fromString("5bfcd54e-135b-4496-97ad-81d0b88b3094")) {
                executor = McHeadExecutor()
            }

            subcommand(I18N_PREFIX.Player.Body.Label, I18N_PREFIX.Player.Body.Description, UUID.fromString("6c32d9c3-5db0-4052-92da-d5fda2faadf6")) {
                executor = McBodyExecutor()
            }

            subcommand(I18N_PREFIX.Player.Onlineuuid.Label, I18N_PREFIX.Player.Onlineuuid.Description, UUID.fromString("497ca9b1-caab-402e-8c7a-b307188c0427")) {
                executor = McUUIDExecutor()
            }

            subcommand(I18N_PREFIX.Player.Offlineuuid.Label, I18N_PREFIX.Player.Offlineuuid.Description, UUID.fromString("3a16c73f-cdc6-4a3f-bbdc-4a3a58bcdbca")) {
                executor = McOfflineUUIDExecutor()
            }
        }

        subcommand(I18N_PREFIX.Sweatshirt.Label, I18N_PREFIX.Sweatshirt.Description, UUID.fromString("3dc081b2-917b-4aeb-8d54-fc13742933a3")) {
            executor = McSkinLorittaSweatshirtExecutor()
        }

        subcommand(I18N_PREFIX.Sparklypower.Label, I18N_PREFIX.Sparklypower.Description, UUID.fromString("f9c8c5f7-9b05-4839-8290-ea98d792443e")) {
            executor = SparklyPowerExecutor()
        }
    }

    private fun convertNonDashedToUniqueID(id: String): UUID {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32))
    }

    inner class SparklyPowerExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Sparklypower.Response1),
                    Emotes.PantufaPickaxe
                )
                styled(
                    context.i18nContext.get(I18N_PREFIX.Sparklypower.Response2("https://discord.gg/sparklypower"))
                )
            }
        }
    }

    inner class McUUIDExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val username = string("player_name", I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val player = args[options.username]

            if (!player.matches(VALID_NAME_REGEX))
                context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(I18N_CATEGORY_PREFIX.InvalidPlayerName(player))
                    )
                }

            val onlineUniqueId = mojang.getUniqueId(player) ?: context.fail(true) {
                styled(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(I18N_CATEGORY_PREFIX.UnknownPlayer(player))
                )
            }

            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Player.Onlineuuid.Result(
                            player,
                            onlineUniqueId.toString()
                        )
                    )
                )
            }
        }
    }

    inner class McOfflineUUIDExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val username = string("player_name", I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val player = args[options.username]

            val uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:$player").toByteArray(Charsets.UTF_8))

            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Player.Offlineuuid.Result(
                            player,
                            uuid.toString()
                        )
                    )
                )
            }
        }
    }

    inner class McSkinLorittaSweatshirtExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val skin = string("skin", I18N_PREFIX.Sweatshirt.Options.Skin.Text)

            val sweatshirtStyle = string("sweatshirt_style", I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Text) {
                choice(
                    I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.Light,
                    MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.LIGHT.name.lowercase()
                )
                choice(
                    I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.Dark,
                    MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.DARK.name.lowercase()
                )
                choice(
                    I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixWavy,
                    MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_WAVY.name.lowercase()
                )
                choice(
                    I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixWavyWithStitches,
                    MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_WAVY_WITH_STITCHES.name.lowercase()
                )
                choice(
                    I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixVertical,
                    MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_VERTICAL.name.lowercase()
                )
                choice(
                    I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixVerticalWithStitches,
                    MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_VERTICAL_WITH_STITCHES.name.lowercase()
                )
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val playerNameOrUrl = args[options.skin]
            val sweatshirtStyleName = args[options.sweatshirtStyle]

            val imageData = if (URLUtils.isValidHttpOrHttpsURL(playerNameOrUrl)) {
                URLImageData(playerNameOrUrl)
            } else {
                if (!playerNameOrUrl.matches(VALID_NAME_REGEX))
                    context.fail(true) {
                        styled(
                            prefix = Emotes.Error,
                            content = context.i18nContext.get(
                                I18N_CATEGORY_PREFIX.InvalidPlayerName(
                                    playerNameOrUrl
                                )
                            )
                        )
                    }

                val profile = mojang.getUserProfileFromName(playerNameOrUrl) ?: context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(I18N_CATEGORY_PREFIX.UnknownPlayer(playerNameOrUrl))
                    )
                }

                // Whether the player has the “Alex?” or “Steve?” skin depends on the Java hashCode of their UUID. Steve is used for even hashes. Example implementations:
                // https://wiki.vg/Mojang_API
                // TODO: This should be migrated to MinecraftMojangAPI instead of using hacky workarounds here
                val uniqueId = convertNonDashedToUniqueID(profile.profileId)
                val isSteve = uniqueId.hashCode() % 2 == 1

                val skinUrl = profile.textures["SKIN"]?.url ?: context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(
                            I18nKeysData.Commands.Command.Minecraft.Player.Skin.PlayerDoesNotHaveASkin(
                                playerName = playerNameOrUrl,
                                skinType = if (isSteve) "Steve" else "Alex"
                            )
                        )
                    )
                }

                URLImageData(skinUrl)
            }

            val image = try {
                loritta.gabrielaImageServerClient.images.minecraftSkinLorittaSweatshirt(
                    MinecraftSkinLorittaSweatshirtRequest(
                        imageData,
                        MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.valueOf(sweatshirtStyleName.uppercase())
                    )
                )
            } catch (e: InvalidMinecraftSkinException) {
                context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(
                            I18nKeysData.Commands.Command.Minecraft.Sweatshirt.InvalidMinecraftSkin
                        )
                    )
                }
            }

            context.reply(true) {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.Command.Minecraft.Sweatshirt.Result
                    ),
                    Emotes.LoriHeart
                )

                files += FileUpload.fromData(image.inputStream(), "skin_lori_sweatshirt.png")
            }
        }

    }

    open inner class McSkinExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val username = string("player_name", I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val player = args[options.username]
            if (!player.matches(VALID_NAME_REGEX))
                context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(I18N_CATEGORY_PREFIX.InvalidPlayerName(player))
                    )
                }

            val profile = mojang.getUserProfileFromName(player) ?: context.fail(true) {
                styled(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(I18N_CATEGORY_PREFIX.UnknownPlayer(player))
                )
            }

            // Whether the player has the “Alex?” or “Steve?” skin depends on the Java hashCode of their UUID. Steve is used for even hashes. Example implementations:
            // https://wiki.vg/Mojang_API
            // TODO: This should be migrated to MinecraftMojangAPI instead of using hacky workarounds here
            val uniqueId = convertNonDashedToUniqueID(profile.profileId)
            val isSteve = uniqueId.hashCode() % 2 == 1

            val skinUrl = profile.textures["SKIN"]?.url ?: context.fail(true) {
                styled(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(
                        I18nKeysData.Commands.Command.Minecraft.Player.Skin.PlayerDoesNotHaveASkin(
                            playerName = player,
                            skinType = if (isSteve) "Steve" else "Alex"
                        )
                    )
                )
            }

            context.reply(false) {
                content = skinUrl
            }
        }
    }

    open inner class CrafatarExecutorBase(val type: String) : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val username = string("player_name", I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val player = args[options.username]
            if (!player.matches(VALID_NAME_REGEX))
                context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(I18N_CATEGORY_PREFIX.InvalidPlayerName(player))
                    )
                }

            val uuid = mojang.getUniqueId(player) ?: context.fail(true) {
                styled(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(I18N_CATEGORY_PREFIX.UnknownPlayer(player))
                )
            }

            context.reply(false) {
                content = "https://crafatar.com/$type/$uuid?size=128&overlay"
            }
        }
    }

    inner class McAvatarExecutor : CrafatarExecutorBase("avatars")
    inner class McBodyExecutor : CrafatarExecutorBase("renders/body")
    inner class McHeadExecutor : CrafatarExecutorBase("renders/head")
}