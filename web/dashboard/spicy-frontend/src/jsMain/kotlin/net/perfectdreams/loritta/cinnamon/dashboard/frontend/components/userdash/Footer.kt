package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.dom.Footer
import org.jetbrains.compose.web.dom.Text

@Composable
fun LegalFooter() {
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.UTC).year

    Footer(
        attrs = {
            classes("legal-footer")
        }
    ) {
        Text(
            "PerfectDreams | CNPJ: 40.713.764/0001-49 | © Todos os direitos reservados. 2017-$currentYear"
        )
    }
}