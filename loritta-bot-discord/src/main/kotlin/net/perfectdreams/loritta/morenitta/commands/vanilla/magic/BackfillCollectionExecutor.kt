package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.cinnamon.pudding.tables.Collections
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.profile.CollectionsManager
import org.jetbrains.exposed.sql.selectAll

object BackfillCollectionExecutor : LoriToolsCommand.LoriToolsExecutor {
    override val args = "backfill collection <internalName>"

    override fun executes(): suspend CommandContext.() -> Boolean = task@{
        if (args.getOrNull(0) != "backfill")
            return@task false
        if (args.getOrNull(1) != "collection")
            return@task false

        val context = checkType<DiscordCommandContext>(this)

        val collectionId = args.getOrNull(2)
        if (collectionId == null) {
            context.reply(
                LorittaReply("Você precisa especificar o nome interno da coleção!")
            )
            return@task true
        }

        val result = loritta.pudding.transaction {
            val collectionExists = Collections.selectAll()
                .where { Collections.id eq collectionId }
                .count() != 0L

            if (!collectionExists)
                return@transaction null

            val userIds = CollectionsManager.findUsersWhoCompletedCollection(loritta.pudding, collectionId)

            var granted = 0
            for (userId in userIds) {
                val profile = loritta._getLorittaProfile(userId) ?: continue
                if (CollectionsManager.tryGrantCollectionReward(loritta.pudding, profile, collectionId))
                    granted++
            }

            BackfillResult(userIds.size, granted)
        }

        if (result == null) {
            context.reply(
                LorittaReply("A coleção `$collectionId` não existe!")
            )
            return@task true
        }

        context.reply(
            LorittaReply(
                "Backfill da coleção `$collectionId` concluído! ${result.granted} usuário(s) receberam as recompensas (${result.completedCount} já possuem todos os itens da coleção)"
            )
        )
        return@task true
    }

    private data class BackfillResult(val completedCount: Int, val granted: Int)
}
