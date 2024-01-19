package com.tonkeeper.core.tonconnect

import android.content.Context
import android.net.Uri
import com.tonkeeper.App
import com.tonkeeper.api.Tonapi
import com.tonkeeper.api.totalFees
import com.tonkeeper.core.Coin
import com.tonkeeper.core.currency.from
import com.tonkeeper.core.currency.ton
import com.tonkeeper.core.formatter.CurrencyFormatter
import com.tonkeeper.core.history.HistoryHelper
import com.tonkeeper.core.tonconnect.models.TCApp
import com.tonkeeper.core.tonconnect.models.TCEvent
import com.tonkeeper.core.tonconnect.models.TCRequest
import com.tonkeeper.core.tonconnect.models.TCTransaction
import com.tonkeeper.core.tonconnect.models.reply.TCBase
import com.tonkeeper.core.tonconnect.models.reply.TCResultError
import com.tonkeeper.core.tonconnect.models.reply.TCResultSuccess
import com.tonkeeper.event.RequestActionEvent
import com.tonkeeper.extensions.emulate
import com.tonkeeper.extensions.sendToBlockchain
import com.tonkeeper.fragment.root.RootActivity
import com.tonkeeper.fragment.tonconnect.auth.TCAuthFragment
import core.EventBus
import io.tonapi.models.EmulateMessageToWalletRequest
import io.tonapi.models.SendBlockchainMessageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.block.AddrStd
import org.ton.block.Message
import org.ton.boc.BagOfCells
import org.ton.cell.buildCell
import org.ton.contract.wallet.WalletContract
import org.ton.contract.wallet.WalletTransfer
import org.ton.crypto.base64
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeTlb
import ton.SupportedTokens
import ton.extensions.base64
import uikit.extensions.activity
import java.nio.charset.Charset

class TonConnect(private val context: Context) {

    companion object {

        fun from(context: Context): TonConnect? {
            val activity = context.activity as? RootActivity ?: return null
            return activity.tonConnect
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val appRepository = AppRepository()
    private val bridge = Bridge()

    private val realtime = Realtime(context) { event ->
        onEvent(event)
    }

    fun onCreate() {
        scope.launch {
            stopEventHandler()
            startEventHandler()
        }
    }

    fun onDestroy() {
        stopEventHandler()
        scope.cancel()
    }

    fun restartEventHandler() {
        stopEventHandler()
        scope.launch { startEventHandler() }
    }

    private suspend fun startEventHandler() {
        val accountId = getAccountId()
        val clientIds = appRepository.getClientIds(accountId)
        realtime.start(clientIds)
    }

    fun isSupportUri(uri: Uri): Boolean {
        if (uri.scheme == "tc") {
            return true
        }
        return uri.host == "app.tonkeeper.com" && uri.path == "/ton-connect" || uri.host == "ton-connect"
    }

    fun resolveScreen(uri: Uri): TCAuthFragment? {
        val request = try {
            TCRequest(uri)
        } catch (e: Throwable) {
            return null
        }

        return TCAuthFragment.newInstance(request)
    }

    private fun onEvent(event: TCEvent) {
        scope.launch(Dispatchers.IO) {
            val app = getApp(event.from) ?: return@launch
            try {
                val msg = app.decrypt(event.body).toString(Charset.defaultCharset())
                val json = JSONObject(msg)
                if (json.getString("method") != "sendTransaction") {
                    return@launch
                }

                val params = json.getJSONArray("params")
                val id = json.getString("id")
                val transfers = TCHelper.createWalletTransfers(params)

                if (transfers.isEmpty()) {
                    return@launch
                }

                val wallet = App.walletManager.getWalletInfo() ?: return@launch
                val response = wallet.emulate(transfers)

                val currency = App.settings.currency
                val items = HistoryHelper.mapping(wallet, response.event, false, true)
                val fee = Coin.toCoins(response.totalFees)
                val feeInCurrency = wallet.ton(fee)
                    .to(currency)

                val feeFormat = "≈ " + CurrencyFormatter.format(SupportedTokens.TON.code, fee) + " · " + CurrencyFormatter.formatFiat(feeInCurrency)

                val transaction = TCTransaction(
                    clientId = app.clientId,
                    id = id,
                    transfers = transfers,
                    fee = feeFormat,
                    previewItems = items,
                )

                EventBus.post(RequestActionEvent(transaction))
            } catch (ignored: Throwable) { }
        }
    }

    fun cancelTransaction(id: String, clientId: String) {
        scope.launch {
            val error = TCResultError(id = id, errorCode = 300, errorMessage = "Reject Request")
            sendEvent(clientId, error)
        }
    }

    suspend fun signTransaction(
        id: String,
        clientId: String,
        transfers: List<WalletTransfer>
    ) = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo() ?: return@withContext
        val privateKey = App.walletManager.getPrivateKey(wallet.id)

        val boc = wallet.sendToBlockchain(privateKey, transfers)?.base64() ?: throw Exception("Error send to blockchain")

        val success = TCResultSuccess(id = id, result = boc)
        sendEvent(clientId, success)
    }

    private suspend fun getAccountId(): String = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo() ?: return@withContext ""
        return@withContext wallet.accountId
    }

    suspend fun sendEvent(clientId: String, event: TCBase) {
        val app = getApp(clientId) ?: return
        bridge.sendEvent(event.toJSON(), app)
    }

    suspend fun sendEvent(clientId: String, event: String) {
        val app = getApp(clientId) ?: return
        bridge.sendEvent(event, app)
    }

    private suspend fun getApp(
        clientId: String
    ): TCApp? = withContext(Dispatchers.IO) {
        val accountId = getAccountId()
        return@withContext appRepository.getApp(accountId, clientId)
    }

    private fun stopEventHandler() {
        realtime.release()
    }

}