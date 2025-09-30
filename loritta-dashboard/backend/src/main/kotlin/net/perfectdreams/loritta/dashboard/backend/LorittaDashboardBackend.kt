package net.perfectdreams.loritta.dashboard.backend

import io.ktor.http.ContentType
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.routing
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.title
import java.io.File

class LorittaDashboardBackend {
    fun start() {
        val server = embeddedServer(CIO, port = 8080) {
            routing {
                get("/") {
                    call.respondHtml {
                        head {
                            title("Loritta Dashboard :3")

                            link(rel = "stylesheet", href = "/assets/css/style.css")
                        }

                        body {
                            div {
                                id = "root"
                            }

                            script(src = "/assets/js/frontend.js") {}
                        }
                    }
                    call.respondText("Loritta is so cute!")
                }

                get("/assets/css/style.css") {
                    call.respondText(
                        File("/home/mrpowergamerbr/IdeaProjects/Loritta/loritta-dashboard/backend/build/sass/style-scss/style.css").readText(),
                        ContentType.Text.CSS
                    )
                }
                get("/assets/js/frontend.js") {
                    call.respondText(File("/home/mrpowergamerbr/IdeaProjects/Loritta/loritta-dashboard/frontend/build/kotlin-webpack/js/developmentExecutable/frontend.js").readText())
                }
            }
        }

        server.start(wait = true)
    }
}