package com.tonapps.tonkeeper.ui.screen.browser.connected.list

import android.net.Uri
import android.util.Log
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.wallet.data.tonconnect.entities.DAppManifestEntity
import com.tonapps.wallet.data.tonconnect.entities.DConnectEntity

data class Item(
    val connect: DConnectEntity,
    val manifest: DAppManifestEntity,
): BaseListItem(0) {

    val icon: Uri
        get() = Uri.parse(manifest.iconUrl)

    val name: String
        get() = manifest.name

    val url: Uri
        get() = Uri.parse(manifest.url)

    val host: String
        get() = url.host!!
}