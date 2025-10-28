package net.perfectdreams.bliss.standalone

import net.perfectdreams.bliss.Bliss
import web.dom.document

class BlissStandalone {
    fun start() {
        Bliss.setupEvents()
        Bliss.processAttributes(document.body)
    }
}