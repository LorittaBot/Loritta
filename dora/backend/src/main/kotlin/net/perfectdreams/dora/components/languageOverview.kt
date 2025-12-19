package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.style
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.luna.modals.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.languageOverview(
    project: Project,
    languageTarget: ResultRow,
    translatedStrings: List<ResultRow>,
    sourceStrings: List<ResultRow>,
) {
    val totalStringsCount = sourceStrings.size
    val translatedStringsCount = translatedStrings.size

    heroWrapper {
        simpleHeroImage("https://upload.wikimedia.org/wikipedia/en/9/99/Dora_the_Explorer_%28character%29.webp")

        heroText {
            h1 {
                text(languageTarget[LanguageTargets.languageName])
            }

            div {
                languageProgressBar(translatedStringsCount, totalStringsCount)
            }
        }
    }

    hr {}

    div {
        discordButtonLink(ButtonStyle.PRIMARY, "/projects/${project.slug}/languages/${languageTarget[LanguageTargets.languageId]}/download") {
            downLoad = "text.yml"

            text("Download Bundle")
        }
    }
}