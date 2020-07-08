package net.perfectdreams.loritta.plugin.htmlprovider

import net.perfectdreams.loritta.sweetmorenitta.AssetHashProvider
import net.perfectdreams.loritta.website.utils.WebsiteAssetsHashes

class JVMAssetHashProvider : AssetHashProvider {
    override fun assetHash(filePath: String) =  WebsiteAssetsHashes.getAssetHash(filePath)
}