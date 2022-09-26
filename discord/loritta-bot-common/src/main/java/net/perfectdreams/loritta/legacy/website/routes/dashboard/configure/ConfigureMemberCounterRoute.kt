package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureMemberCounterRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/member-counter", "member_counter", "configure_member_counter.html")