package net.perfectdreams.loritta.platform.cli.builder

import net.perfectdreams.loritta.common.builder.LorittaReplyBuilder

class CLILorittaReplyBuilder : LorittaReplyBuilder() {
    override fun build(): String {
        return "$prefix | $content"
    }
}