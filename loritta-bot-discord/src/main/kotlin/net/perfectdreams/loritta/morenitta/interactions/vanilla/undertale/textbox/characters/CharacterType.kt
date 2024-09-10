package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.UndertaleCommand

enum class CharacterType(
    val data: CharacterData,
    val charName: StringI18nData,
    val universe: UniverseType,
    val defaultKeyName: String,
    val emote: Emote? = null
) {
    // ===[ DELTARUNE ]===
    DELTARUNE_RALSEI(DeltaruneRalsei, UndertaleCommand.I18N_TEXTBOX_PREFIX.Ralsei.Name, UniverseType.DELTARUNE, "deltarune/ralsei/neutral", Emotes.DeltaruneRalseiNeutral),
    DELTARUNE_RALSEI_WITH_HAT(DeltaruneRalseiWithHat, UndertaleCommand.I18N_TEXTBOX_PREFIX.RalseiWithHat.Name, UniverseType.DELTARUNE, "deltarune/ralsei_with_hat/neutral", Emotes.DeltaruneRalseiWithHatNeutral),
    DELTARUNE_ALPHYS(DeltaruneAlphys, UndertaleCommand.I18N_TEXTBOX_PREFIX.Alphys.Name, UniverseType.DELTARUNE, "deltarune/alphys/neutral", Emotes.DeltaruneAlphysNeutral),
    DELTARUNE_SUSIE(DeltaruneSusie, UndertaleCommand.I18N_TEXTBOX_PREFIX.Susie.Name, UniverseType.DELTARUNE, "deltarune/susie/neutral", Emotes.DeltaruneSusieNeutral),
    DELTARUNE_QUEEN(DeltaruneQueen, UndertaleCommand.I18N_TEXTBOX_PREFIX.Queen.Name, UniverseType.DELTARUNE, "deltarune/queen/neutral", Emotes.DeltaruneQueenNeutral),
    DELTARUNE_LANCER(DeltaruneLancer, UndertaleCommand.I18N_TEXTBOX_PREFIX.Lancer.Name, UniverseType.DELTARUNE, "deltarune/lancer/neutral", Emotes.DeltaruneLancerNeutral),
    DELTARUNE_NOELLE(DeltaruneNoelle, UndertaleCommand.I18N_TEXTBOX_PREFIX.Noelle.Name, UniverseType.DELTARUNE, "deltarune/noelle/neutral", Emotes.DeltaruneNoelleNeutral),
    DELTARUNE_BERDLY(DeltaruneBerdly, UndertaleCommand.I18N_TEXTBOX_PREFIX.Berdly.Name, UniverseType.DELTARUNE, "deltarune/berdly/neutral", Emotes.DeltaruneBerdlyNeutral),
    DELTARUNE_BERDLY_DARK_WORLD(DeltaruneBerdlyDarkWorld, UndertaleCommand.I18N_TEXTBOX_PREFIX.BerdlyDarkWorld.Name, UniverseType.DELTARUNE, "deltarune/berdly_dark_world/neutral", Emotes.DeltaruneBerdlyDarkWorldNeutral),

    // ===[ UNDERTALE ]===
    UNDERTALE_ALPHYS(UndertaleAlphys, UndertaleCommand.I18N_TEXTBOX_PREFIX.Alphys.Name, UniverseType.UNDERTALE, "undertale/alphys/neutral", Emotes.UndertaleAlphysNeutral),
    UNDERTALE_FLOWEY(UndertaleFlowey, UndertaleCommand.I18N_TEXTBOX_PREFIX.Flowey.Name, UniverseType.UNDERTALE, "undertale/flowey/neutral", Emotes.UndertaleFloweyNeutral),
    UNDERTALE_METTATON(UndertaleMettaton, UndertaleCommand.I18N_TEXTBOX_PREFIX.Mettaton.Name, UniverseType.UNDERTALE, "undertale/mettaton_ex/neutral", Emotes.UndertaleMettatonExNeutral),
    UNDERTALE_PAPYRUS(UndertalePapyrus, UndertaleCommand.I18N_TEXTBOX_PREFIX.Papyrus.Name, UniverseType.UNDERTALE, "undertale/papyrus/neutral", Emotes.UndertalePapyrusNeutral),
    UNDERTALE_SANS(UndertaleSans, UndertaleCommand.I18N_TEXTBOX_PREFIX.Sans.Name, UniverseType.UNDERTALE, "undertale/sans/neutral", Emotes.UndertaleSansNeutral),
    UNDERTALE_TORIEL(UndertaleToriel, UndertaleCommand.I18N_TEXTBOX_PREFIX.Toriel.Name, UniverseType.UNDERTALE, "undertale/toriel/neutral", Emotes.UndertaleTorielNeutral),
    UNDERTALE_UNDYNE(UndertaleUndyne, UndertaleCommand.I18N_TEXTBOX_PREFIX.Undyne.Name, UniverseType.UNDERTALE, "undertale/undyne/neutral", Emotes.UndertaleUndyneNeutral),
    UNDERTALE_ASGORE(UndertaleAsgore, UndertaleCommand.I18N_TEXTBOX_PREFIX.Asgore.Name, UniverseType.UNDERTALE, "undertale/asgore/neutral", Emotes.UndertaleAsgoreNeutral),
    UNDERTALE_ASRIEL(UndertaleAsriel, UndertaleCommand.I18N_TEXTBOX_PREFIX.Asriel.Name, UniverseType.UNDERTALE, "undertale/asriel/neutral", Emotes.UndertaleAsrielNeutral)
}