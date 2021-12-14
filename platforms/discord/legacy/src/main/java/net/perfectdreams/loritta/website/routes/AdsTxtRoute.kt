package net.perfectdreams.loritta.website.routes

import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.response.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute

class AdsTxtRoute(private val loritta: LorittaDiscord) : BaseRoute("/ads.txt") {
    override suspend fun onRequest(call: ApplicationCall) {
        if (System.currentTimeMillis() - lastJoinedCachedAds >= 900_000) {
            // Check again
            lastJoinedCachedAds = System.currentTimeMillis()
            GlobalScope.launch {
                val lines = mutableListOf<String>()
                for (adsTxtSource in adsTxtExternalSources) {
                    lines.addAll(
                        loritta.http.get<String>(adsTxtSource)
                            .lines()
                    )
                }
                joinedCachedAdsTxtExternalSources.value = lines
            }
        }

        // Should be always non-null if it is present
        val joinedLines = joinedCachedAdsTxtExternalSources.first { it != null }!!

        call.respondText(
            (joinedLines + rawAdsTxtSources)
                .joinToString("\n")
        )
    }

    companion object {
        private val adsTxtExternalSources = listOf(
            "https://api.nitropay.com/v1/ads-595.txt"
        )

        private var lastJoinedCachedAds = 0L
        private var joinedCachedAdsTxtExternalSources = MutableStateFlow<List<String>?>(null)

        private val rawAdsTxtSources = """vidoomy.com, 62362, DIRECT
google.com, pub-2831120411392012, RESELLER, f08c47fec0942fa0
tremorhub.com, 4cywq-a04wk, RESELLER, 1a4e959a1b50034a
yieldmo.com, 2731991718797714293, DIRECT
adform.com, 2742, RESELLER
advertising.com, 22762, RESELLER
aol.com, 22762, RESELLER
adtech.com, 11303, RESELLER
advertising.com, 26631, RESELLER
appnexus.com, 12475, RESELLER, f5ab79cb980f11d1
pubmatic.com, 156498, RESELLER, 5d62403b186f2ace
freewheel.tv, 872257, RESELLER
freewheel.tv, 894193, RESELLER
openx.com, 540804929, RESELLER, 6a698e2ec38604c6
lkqd.net, 430, RESELLER, 59c49fa9598a0117
lkqd.com, 430, RESELLER, 59c49fa9598a0117
districtm.io, 101540, RESELLER, 3fd707be9c4527c3
appnexus.com, 1908, RESELLER, f5ab79cb980f11d1
spotxchange.com, 218443, RESELLER, 7842df1d2fe2db34
spotx.tv, 218443, RESELLER, 7842df1d2fe2db34
emxdgt.com, 1495, RESELLER, 1e1d41537f7cad7f
appnexus.com, 1356, RESELLER, f5ab79cb980f11d1
beachfront.com, 6547, RESELLER
smartadserver.com, 3136, RESELLER
contextweb.com, 560288, RESELLER, 89ff185a4c4e857c
pubmatic.com, 156439, RESELLER
pubmatic.com, 154037, RESELLER
rubiconproject.com, 16114, RESELLER, 0bfd66d529a55807
openx.com, 537149888, RESELLER, 6a698e2ec38604c6
sovrn.com, 257611, RESELLER, fafdf38b16bf6b2b
appnexus.com, 3703, RESELLER, f5ab79cb980f11d1
smartadserver.com, 1963, RESELLER
smartadserver.com, 3276, RESELLER
improvedigital.com, 1738, RESELLER
conversantmedia.com, 100112, DIRECT, 03113cd04947736d
appnexus.com, 4052, RESELLER
aol.com, 55011, RESELLER, e1a5b5b6e3255540
contextweb.com, 561998, RESELLER, 89ff185a4c4e857c
openx.com, 540031703, RESELLER, 6a698e2ec38604c6
pubmatic.com, 158100, RESELLER, 5d62403b186f2ace
indexexchange.com, 192311, RESELLER
indexexchange.com, 193069, RESELLER
rubiconproject.com, 20744, RESELLER, 0bfd66d529a55807
openx.com, 540298543, RESELLER, 6a698e2ec38604c6
loopme.com, 11058, RESELLER, 6c8d5f95897a5a3b
google.com, pub-7995104076770938, DIRECT, f08c47fec0942fa0
appnexus.com, 8790, RESELLER, f5ab79cb980f11d1
indexexchange.com, 187924, DIRECT
indexexchange.com, 189458, DIRECT
openx.com, 540322758, RESELLER, 6a698e2ec38604c6
pubmatic.com, 157102, RESELLER, 5d62403b186f2ace
pubmatic.com, 157163, RESELLER, 5d62403b186f2ace
pubmatic.com, 157752, RESELLER, 5d62403b186f2ace
rubiconproject.com, 13894, RESELLER, 0bfd66d529a55807
rubiconproject.com, 18008, RESELLER, 0bfd66d529a55807
yahoo.com, 56860, RESELLER, e1a5b5b6e3255540
aol.com, 56860, RESELLER, e1a5b5b6e3255540
rhythmone.com, 274200170, RESELLER, a670c89d4a324e47
video.unrulymedia.com, 274200170, RESELLER, a670c89d4a324e47""".lines()
    }
}