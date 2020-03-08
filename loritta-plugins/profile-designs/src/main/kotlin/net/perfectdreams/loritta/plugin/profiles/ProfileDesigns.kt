package net.perfectdreams.loritta.plugin.profiles

import com.mrpowergamerbr.loritta.profile.ProfileDesign
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.utils.Rarity
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.profiles.designs.*

class ProfileDesigns(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    val registeredProfiles = mutableListOf<ProfileDesign>()

    override fun onEnable() {
        registeredProfiles.add(
                ProfileDesign(
                        false,
                        DebugProfileCreator::class.java,
                        "debug",
                        Rarity.COMMON,
                        listOf(),
                        false,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainWhiteProfileCreator::class.java,
                        "plain_white",
                        Rarity.UNCOMMON,
                        listOf(321821618177310721L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainOrangeProfileCreator::class.java,
                        "plain_orange",
                        Rarity.UNCOMMON,
                        listOf(321821618177310721L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainPurpleProfileCreator::class.java,
                        "plain_purple",
                        Rarity.UNCOMMON,
                        listOf(321821618177310721L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainAquaProfileCreator::class.java,
                        "plain_aqua",
                        Rarity.UNCOMMON,
                        listOf(321821618177310721L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainGreenProfileCreator::class.java,
                        "plain_green",
                        Rarity.UNCOMMON,
                        listOf(321821618177310721L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainGreenHeartsProfileCreator::class.java,
                        "plain_green_hearts",
                        Rarity.UNCOMMON,
                        listOf(321821618177310721L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        CowboyProfileCreator::class.java,
                        "cowboy",
                        Rarity.RARE,
                        listOf(321821618177310721L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        NextGenProfileCreator::class.java,
                        "next_gen",
                        Rarity.RARE,
                        listOf(361977144445763585L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        MonicaAtaProfileCreator::class.java,
                        "monica_ata",
                        Rarity.EPIC,
                        listOf(321821618177310721L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        UndertaleProfileCreator::class.java,
                        "undertale",
                        Rarity.EPIC,
                        listOf(419125869475397633L),
                        true,
                        false
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        LoriAtaProfileCreator::class.java,
                        "lori_ata",
                        Rarity.EPIC,
                        listOf(321821618177310721L, 419125869475397633L),
                        true,
                        false
                )
        )

        registeredProfiles.add(
                ProfileDesign(
                        true,
                        Halloween2019ProfileCreator::class.java,
                        "halloween_2019",
                        Rarity.LEGENDARY,
                        listOf(),
                        false,
                        true
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        Christmas2019ProfileCreator::class.java,
                        "christmas_2019",
                        Rarity.LEGENDARY,
                        listOf(),
                        false,
                        true
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        LorittaChristmas2019ProfileCreator::class.java,
                        "animated_christmas_2019",
                        Rarity.LEGENDARY,
                        listOf(),
                        false,
                        true
                )
        )

        registeredProfiles.forEach {
            lorittaDiscord.profileDesignManager.registerDesign(it)
        }
    }

    override fun onDisable() {
        super.onDisable()

        registeredProfiles.forEach {
            lorittaDiscord.profileDesignManager.unregisterDesign(it)
        }
    }
}
