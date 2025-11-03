package net.perfectdreams.loritta.morenitta.website.views.dashboard

object DashboardView {
    /**
     * JavaScript snippet that, when clicking on the element, the `is-open` class will be removed and the `is-closed` class will be added
     */
    // language=JavaScript
    val JAVASCRIPT_CLOSE_LEFT_SIDEBAR_ON_CLICK = """
                        self.on("click", (e) => {
                            const leftSidebar = selectFirst("#left-sidebar");
                            leftSidebar.removeClass("is-open")
                            leftSidebar.addClass("is-closed")
                        })
                    """.trimIndent()
}