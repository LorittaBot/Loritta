package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.DiscordUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.DiscordChannel
import net.perfectdreams.loritta.serializable.GuildMessageChannel
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun DiscordChannelSelectMenu(
    m: LorittaDashboardFrontend,
    i18nContext: I18nContext,
    entries: List<DiscordChannel>,
    selectedChannelId: Long?,
    onSelect: (GuildMessageChannel) -> (Unit)
) {
    SelectMenu(
        placeholder = "Selecione um Canal",
        entries = entries.filterIsInstance<GuildMessageChannel>()
            .map {
                SelectMenuEntry(
                    {
                        TextWithIconWrapper(DiscordUtils.getIconForChannel(it), svgAttrs = {
                            attr("style", "height: 1em;")
                        }) {
                            Text(it.name)
                            if (LorittaDashboardFrontend.shouldChannelSelectMenuPermissionCheckAlwaysFail || !it.canTalk) {
                                Text(" ")
                                Span(attrs = {
                                    classes("tag", "warn")
                                }) {
                                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.ChannelNoTalkPermissionModal.Title))
                                }
                            }
                        }
                    },
                    it.id == selectedChannelId,
                    {
                        if (LorittaDashboardFrontend.shouldChannelSelectMenuPermissionCheckAlwaysFail || !it.canTalk) {
                            // Open menu explaining why you can't select this channel
                            m.globalState.openCloseOnlyModal(i18nContext.get(I18nKeysData.Website.Dashboard.ChannelNoTalkPermissionModal.Title), true) {
                                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.ChannelNoTalkPermissionModal.Description)) {
                                    P {
                                        Text(line)
                                    }
                                }
                            }
                            return@SelectMenuEntry
                        }
                        onSelect.invoke(it)
                    },
                    {}
                )
            }
    )
}