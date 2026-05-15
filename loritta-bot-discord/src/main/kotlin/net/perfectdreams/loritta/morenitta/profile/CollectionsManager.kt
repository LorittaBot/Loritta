package net.perfectdreams.loritta.morenitta.profile

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectionItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectionRewardBackgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectionRewardProfileDesigns
import net.perfectdreams.loritta.cinnamon.pudding.tables.Collections
import net.perfectdreams.loritta.cinnamon.pudding.tables.CompletedCollections
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredFinishedCollectionTransaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.OffsetDateTime

object CollectionsManager {
    /**
     * Checks if the user owns every background and profile design that is part of [collectionId].
     */
    suspend fun hasCompletedCollection(pudding: Pudding, userId: Long, collectionId: String): Boolean {
        return pudding.transaction {
            val items = CollectionItems.selectAll()
                .where { CollectionItems.collection eq collectionId }
                .toList()

            val requiredBackgrounds = items.mapNotNull { it[CollectionItems.background]?.value }
            val requiredProfileDesigns = items.mapNotNull { it[CollectionItems.profileDesign]?.value }

            // An empty collection is never considered "completed"
            if (requiredBackgrounds.isEmpty() && requiredProfileDesigns.isEmpty())
                return@transaction false

            if (requiredBackgrounds.isNotEmpty()) {
                val ownedBackgrounds = BackgroundPayments.select(BackgroundPayments.background)
                    .where { (BackgroundPayments.userId eq userId) and (BackgroundPayments.background inList requiredBackgrounds) }
                    .map { it[BackgroundPayments.background].value }
                    .toSet()

                if (!ownedBackgrounds.containsAll(requiredBackgrounds))
                    return@transaction false
            }

            if (requiredProfileDesigns.isNotEmpty()) {
                val ownedProfileDesigns = ProfileDesignsPayments.select(ProfileDesignsPayments.profile)
                    .where { (ProfileDesignsPayments.userId eq userId) and (ProfileDesignsPayments.profile inList requiredProfileDesigns) }
                    .map { it[ProfileDesignsPayments.profile].value }
                    .toSet()

                if (!ownedProfileDesigns.containsAll(requiredProfileDesigns))
                    return@transaction false
            }

            true
        }
    }

    /**
     * Returns the IDs of every user that currently owns all of [collectionId]'s items, regardless of whether they have already claimed the collection's rewards.
     * Useful for backfilling rewards for collections created after users had already obtained the items.
     */
    suspend fun findUsersWhoCompletedCollection(pudding: Pudding, collectionId: String): Set<Long> {
        return pudding.transaction {
            val items = CollectionItems.selectAll()
                .where { CollectionItems.collection eq collectionId }
                .toList()

            val requiredBackgrounds = items.mapNotNull { it[CollectionItems.background]?.value }
            val requiredProfileDesigns = items.mapNotNull { it[CollectionItems.profileDesign]?.value }

            if (requiredBackgrounds.isEmpty() && requiredProfileDesigns.isEmpty())
                return@transaction emptySet()

            val backgroundsByUser = if (requiredBackgrounds.isEmpty()) emptyMap() else
                BackgroundPayments.select(BackgroundPayments.userId, BackgroundPayments.background)
                    .where { BackgroundPayments.background inList requiredBackgrounds }
                    .groupBy({ it[BackgroundPayments.userId] }, { it[BackgroundPayments.background].value })
                    .mapValues { it.value.toSet() }

            val profileDesignsByUser = if (requiredProfileDesigns.isEmpty()) emptyMap() else
                ProfileDesignsPayments.select(ProfileDesignsPayments.userId, ProfileDesignsPayments.profile)
                    .where { ProfileDesignsPayments.profile inList requiredProfileDesigns }
                    .groupBy({ it[ProfileDesignsPayments.userId] }, { it[ProfileDesignsPayments.profile].value })
                    .mapValues { it.value.toSet() }

            val candidateUserIds = backgroundsByUser.keys + profileDesignsByUser.keys

            candidateUserIds.filterTo(mutableSetOf()) { userId ->
                (backgroundsByUser[userId] ?: emptySet()).containsAll(requiredBackgrounds) &&
                    (profileDesignsByUser[userId] ?: emptySet()).containsAll(requiredProfileDesigns)
            }
        }
    }

    /**
     * If [profile] has completed the (enabled) collection [collectionId] and hasn't claimed it yet, grants its rewards (sonhos + reward backgrounds + reward profile designs) exactly once.
     *
     * @return true if the rewards were granted, false otherwise (collection missing/disabled, not completed, or already claimed)
     */
    suspend fun tryGrantCollectionReward(pudding: Pudding, profile: Profile, collectionId: String): Boolean {
        return pudding.transaction {
            val collection = Collections.selectAll()
                .where { (Collections.id eq collectionId) and (Collections.enabled eq true) }
                .firstOrNull() ?: return@transaction false

            val userId = profile.userId

            if (!hasCompletedCollection(pudding, userId, collectionId))
                return@transaction false

            val alreadyClaimed = CompletedCollections.selectAll()
                .where { (CompletedCollections.userId eq userId) and (CompletedCollections.collection eq collectionId) }
                .count() != 0L

            if (alreadyClaimed)
                return@transaction false

            CompletedCollections.insert {
                it[CompletedCollections.userId] = userId
                it[CompletedCollections.collection] = collectionId
                it[CompletedCollections.claimedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
            }

            val rewardSonhos = collection[Collections.rewardSonhos]
            if (rewardSonhos > 0L) {
                profile.addSonhosAndAddToTransactionLogNested(
                    rewardSonhos,
                    SonhosPaymentReason.COLLECTION_REWARD
                )
            }

            SimpleSonhosTransactionsLogUtils.insert(
                userId,
                Instant.now(),
                TransactionType.COLLECTION,
                rewardSonhos,
                StoredFinishedCollectionTransaction(collectionId)
            )

            val rewardBackgrounds = CollectionRewardBackgrounds.select(CollectionRewardBackgrounds.background)
                .where { CollectionRewardBackgrounds.collection eq collectionId }
                .map { it[CollectionRewardBackgrounds.background].value }

            for (background in rewardBackgrounds) {
                val alreadyOwned = BackgroundPayments.selectAll()
                    .where { (BackgroundPayments.userId eq userId) and (BackgroundPayments.background eq background) }
                    .count() != 0L

                if (alreadyOwned)
                    continue

                BackgroundPayments.insert {
                    it[BackgroundPayments.userId] = userId
                    it[BackgroundPayments.background] = background
                    it[BackgroundPayments.boughtAt] = System.currentTimeMillis()
                    it[BackgroundPayments.cost] = 0L
                }
            }

            val rewardProfileDesigns = CollectionRewardProfileDesigns.select(CollectionRewardProfileDesigns.profileDesign)
                .where { CollectionRewardProfileDesigns.collection eq collectionId }
                .map { it[CollectionRewardProfileDesigns.profileDesign].value }

            for (profileDesign in rewardProfileDesigns) {
                val alreadyOwned = ProfileDesignsPayments.selectAll()
                    .where { (ProfileDesignsPayments.userId eq userId) and (ProfileDesignsPayments.profile eq profileDesign) }
                    .count() != 0L

                if (alreadyOwned)
                    continue

                ProfileDesignsPayments.insert {
                    it[ProfileDesignsPayments.userId] = userId
                    it[ProfileDesignsPayments.profile] = profileDesign
                    it[ProfileDesignsPayments.boughtAt] = System.currentTimeMillis()
                    it[ProfileDesignsPayments.cost] = 0L
                }
            }

            true
        }
    }

    /**
     * Finds every collection that includes the just-obtained item and, for any collection the user has now completed and not yet claimed, grants its rewards exactly once.
     */
    suspend fun giveCollectionRewardsIfNewlyCompleted(
        pudding: Pudding,
        profile: Profile,
        obtainedBackground: String? = null,
        obtainedProfileDesign: String? = null
    ) {
        pudding.transaction {
            val affectedCollectionIds = mutableSetOf<String>()

            if (obtainedBackground != null) {
                affectedCollectionIds += CollectionItems.select(CollectionItems.collection)
                    .where { CollectionItems.background eq obtainedBackground }
                    .map { it[CollectionItems.collection].value }
            }

            if (obtainedProfileDesign != null) {
                affectedCollectionIds += CollectionItems.select(CollectionItems.collection)
                    .where { CollectionItems.profileDesign eq obtainedProfileDesign }
                    .map { it[CollectionItems.collection].value }
            }

            for (collectionId in affectedCollectionIds) {
                tryGrantCollectionReward(pudding, profile, collectionId)
            }
        }
    }
}
