package com.tonapps.tonkeeper.ui.screen.browser.connected.list

import android.view.ViewGroup
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class Adapter: BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return AppHolder(parent)
    }
}