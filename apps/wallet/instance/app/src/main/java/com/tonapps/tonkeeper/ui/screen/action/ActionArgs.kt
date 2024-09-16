package com.tonapps.tonkeeper.ui.screen.action

import android.os.Bundle
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.wallet.data.core.entity.RawMessageEntity
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import uikit.base.BaseArgs

data class ActionArgs(
    val walletId: String,
    val request: SignRequestEntity,
    val historyItems: List<HistoryItem>,
    val resultKey: String,
    val isBattery: Boolean,
): BaseArgs() {

    private companion object {
        private const val ARG_WALLET_ID = "wallet_id"
        private const val ARG_HISTORY_ITEMS = "history_items"
        private const val ARG_RESULT_KEY = "result_key"
        private const val ARG_REQUEST = "request"
        private const val ARG_IS_BATTERY = "is_battery"
    }

    val validUntil: Long
        get() = request.validUntil

    val messages: List<RawMessageEntity>
        get() = request.messages

    constructor(bundle: Bundle) : this(
        walletId = bundle.getString(ARG_WALLET_ID)!!,
        request = bundle.getParcelable(ARG_REQUEST)!!,
        historyItems = bundle.getParcelableArrayList(ARG_HISTORY_ITEMS)!!,
        resultKey = bundle.getString(ARG_RESULT_KEY)!!,
        isBattery = bundle.getBoolean(ARG_IS_BATTERY)
    )

    override fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(ARG_WALLET_ID, walletId)
        bundle.putParcelable(ARG_REQUEST, request)
        bundle.putParcelableArrayList(ARG_HISTORY_ITEMS, ArrayList(historyItems))
        bundle.putString(ARG_RESULT_KEY, resultKey)
        bundle.putBoolean(ARG_IS_BATTERY, isBattery)
        return bundle
    }
}