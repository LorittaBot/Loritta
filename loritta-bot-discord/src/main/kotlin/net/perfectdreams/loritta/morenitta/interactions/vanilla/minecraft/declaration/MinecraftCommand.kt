package net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration

import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.CrafatarExecutorBase
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.MCAchievementExecutor
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.MCOfflineUUIDExecutor
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.MCSkinExecutor
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.MCSkinLorittaSweatshirtExecutor
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.MCUUIDExecutor
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.SparklyPowerExecutor
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import java.util.UUID

class MinecraftCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Minecraft
        val I18N_CATEGORY_PREFIX = I18nKeysData.Commands.Category.Minecraft
        val VALID_NAME_REGEX = Regex("[a-zA-Z0-9_]{2,16}")
        val mojang = MinecraftMojangAPI()

        fun convertNonDashedToUniqueID(id: String): UUID {
            return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32))
        }
    }

    override fun command() = slashCommand(
        I18N_PREFIX.Label,
        I18N_CATEGORY_PREFIX.Name /* TODO: Use the category description */,
        CommandCategory.MINECRAFT,
        UUID.fromString("4b218716-6dbd-48c1-93fb-8f389c2f635e")
    ) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommandGroup(I18N_PREFIX.Player.Label, I18N_PREFIX.Player.Description) {
            subcommand(
                I18N_PREFIX.Player.Skin.Label,
                I18N_PREFIX.Player.Skin.Description,
                UUID.fromString("df20d1dc-26cc-4895-a17c-9fa9417d869a")
            ) {
                enableLegacyMessageSupport = true

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("mcskin")
                    add("skinstealer")
                    add("skinsteal")
                }

                examples = I18N_PREFIX.Player.Skin.Examples

                executor = MCSkinExecutor()
            }

            subcommand(
                I18N_PREFIX.Player.Avatar.Label,
                I18N_PREFIX.Player.Avatar.Description,
                UUID.fromString("fb5e21b5-fa3c-4abf-bf45-6a03e8b3f0c2")
            ) {
                enableLegacyMessageSupport = true

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("mcavatar")
                }

                examples = I18N_PREFIX.Player.Avatar.Examples

                executor = MCAvatarExecutor()
            }

            subcommand(
                I18N_PREFIX.Player.Head.Label,
                I18N_PREFIX.Player.Head.Description,
                UUID.fromString("5bfcd54e-135b-4496-97ad-81d0b88b3094")
            ) {
                enableLegacyMessageSupport = true

                examples = I18N_PREFIX.Player.Head.Examples

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("mchead")
                }

                executor = MCHeadExecutor()
            }

            subcommand(
                I18N_PREFIX.Player.Body.Label,
                I18N_PREFIX.Player.Body.Description,
                UUID.fromString("6c32d9c3-5db0-4052-92da-d5fda2faadf6")
            ) {
                enableLegacyMessageSupport = true

                examples = I18N_PREFIX.Player.Body.Examples

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("mcbody")
                }

                executor = MCBodyExecutor()
            }

            subcommand(
                I18N_PREFIX.Player.Onlineuuid.Label,
                I18N_PREFIX.Player.Onlineuuid.Description,
                UUID.fromString("497ca9b1-caab-402e-8c7a-b307188c0427")
            ) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("mcuuid")
                }

                executor = MCUUIDExecutor()
            }

            subcommand(
                I18N_PREFIX.Player.Offlineuuid.Label,
                I18N_PREFIX.Player.Offlineuuid.Description,
                UUID.fromString("3a16c73f-cdc6-4a3f-bbdc-4a3a58bcdbca")
            ) {
                enableLegacyMessageSupport = true

                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("mcofflineuuid")
                }

                executor = MCOfflineUUIDExecutor()
            }
        }

        subcommand(
            I18N_PREFIX.Sweatshirt.Label,
            I18N_PREFIX.Sweatshirt.Description,
            UUID.fromString("3dc081b2-917b-4aeb-8d54-fc13742933a3")
        ) {
            enableLegacyMessageSupport = true

            alternativeLegacyAbsoluteCommandPaths.apply {
                add("mcmoletom")
                add("mcsweater")
            }

            examples = I18N_PREFIX.Sweatshirt.Examples

            executor = MCSkinLorittaSweatshirtExecutor(loritta)
        }

        subcommand(
            I18N_PREFIX.Achievement.Label,
            I18N_PREFIX.Achievement.Description,
            UUID.fromString("4d7c055f-931b-42b1-81d4-fadb63442d17")
        ) {
            enableLegacyMessageSupport = true

            alternativeLegacyAbsoluteCommandPaths.apply {
                add("mcconquista")
            }

            examples = I18N_PREFIX.Achievement.Examples

            executor = MCAchievementExecutor()
        }

        subcommand(
            I18N_PREFIX.Sparklypower.Label,
            I18N_PREFIX.Sparklypower.Description,
            UUID.fromString("f9c8c5f7-9b05-4839-8290-ea98d792443e")
        ) {
            enableLegacyMessageSupport = true

            alternativeLegacyAbsoluteCommandPaths.apply {
                add("sparklypower")
            }

            executor = SparklyPowerExecutor()
        }
    }

    inner class MCAvatarExecutor : CrafatarExecutorBase(loritta, "avatars")
    inner class MCBodyExecutor : CrafatarExecutorBase(loritta, "renders/body")
    inner class MCHeadExecutor : CrafatarExecutorBase(loritta, "renders/head")
}