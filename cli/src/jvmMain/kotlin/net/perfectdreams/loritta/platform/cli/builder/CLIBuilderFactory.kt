package net.perfectdreams.loritta.platform.cli.builder

import net.perfectdreams.loritta.common.builder.BuilderFactory
import net.perfectdreams.loritta.common.builder.LorittaMultiReplyBuilder

object CLIBuilderFactory : BuilderFactory {
    override fun createReplyBuilder() = CLILorittaReplyBuilder()
    override fun createMultiReplyBuilder() = LorittaMultiReplyBuilder(this)
}