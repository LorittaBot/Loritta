package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.ApplicationCall
import kotlinx.html.h1
import kotlinx.html.hr
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectDashboardSection
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.ButtonStyle
import net.perfectdreams.dora.components.dashboardBase
import net.perfectdreams.dora.components.discordButton
import net.perfectdreams.dora.components.heroText
import net.perfectdreams.dora.components.heroWrapper
import net.perfectdreams.dora.components.projectLeftSidebarEntries
import net.perfectdreams.dora.components.simpleHeroImage
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.utils.respondHtml

class ProjectSyncRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/sync") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    projectLeftSidebarEntries(
                        project,
                        ProjectDashboardSection.SYNC
                    )
                }
            ) {
                heroWrapper {
                    simpleHeroImage("https://upload.wikimedia.org/wikipedia/en/9/99/Dora_the_Explorer_%28character%29.webp")

                    heroText {
                        h1 {
                            text("Sincronizar")
                        }
                    }
                }

                hr {}

                // Sync actions
                discordButton(ButtonStyle.PRIMARY) {
                    attributes["bliss-post"] = "/projects/${project.slug}/pull"
                    text("Pull")
                }

                discordButton(ButtonStyle.PRIMARY) {
                    attributes["bliss-post"] = "/projects/${project.slug}/push"
                    text("Push")
                }
            }
        }
    }
}
