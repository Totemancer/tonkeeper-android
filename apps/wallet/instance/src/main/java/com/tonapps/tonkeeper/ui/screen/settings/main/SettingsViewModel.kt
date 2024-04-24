package com.tonapps.tonkeeper.ui.screen.settings.main

import android.os.Build
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.extensions.capitalized
import com.tonapps.tonkeeper.ui.screen.settings.main.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import uikit.extensions.collectFlow

class SettingsViewModel(
    private val walletRepository: WalletRepository,
    private val settings: SettingsRepository,
    private val api: API
): ViewModel() {

    private data class Data(
        val wallet: WalletEntity? = null,
        val currency: WalletCurrency? = null,
        val language: Language? = null
    )

    private val _dataFlow = MutableStateFlow(Data())
    private val dataFlow = _dataFlow.asStateFlow().filter { it.wallet != null && it.currency != null }

    private val _uiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    init {
        collectFlow(walletRepository.activeWalletFlow) { _dataFlow.value = _dataFlow.value.copy(wallet = it) }
        collectFlow(settings.currencyFlow) { _dataFlow.value = _dataFlow.value.copy(currency = it) }
        collectFlow(settings.languageFlow) { _dataFlow.value = _dataFlow.value.copy(language = it) }

        collectFlow(dataFlow) {
            val wallet = it.wallet ?: return@collectFlow
            val currency = it.currency ?: return@collectFlow
            val language = it.language ?: return@collectFlow
            buildUiItems(wallet, currency, language)
        }
    }

    fun signOut() {
        walletRepository.removeCurrent()
    }

    private fun buildUiItems(
        wallet: WalletEntity,
        currency: WalletCurrency,
        language: Language
    ) {
        val uiItems = mutableListOf<Item>()
        uiItems.add(Item.Account(wallet))

        uiItems.add(Item.Space)
        uiItems.add(Item.Security(ListCell.Position.FIRST))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            uiItems.add(Item.Widget(ListCell.Position.MIDDLE))
        }
        uiItems.add(Item.Theme(ListCell.Position.MIDDLE))
        uiItems.add(Item.Currency(currency.code, ListCell.Position.MIDDLE))
        uiItems.add(Item.Language(language.nameLocalized.capitalized, ListCell.Position.LAST))

        uiItems.add(Item.Space)
        uiItems.add(Item.Support(ListCell.Position.FIRST, api.config.directSupportUrl))
        uiItems.add(Item.News(ListCell.Position.MIDDLE, api.config.tonkeeperNewsUrl))
        uiItems.add(Item.Contact(ListCell.Position.MIDDLE, api.config.supportLink))
        uiItems.add(Item.Legal(ListCell.Position.LAST))

        uiItems.add(Item.Space)
        if (wallet.type == WalletType.Watch) {
            uiItems.add(Item.DeleteWatchAccount(ListCell.Position.SINGLE))
        } else {
            uiItems.add(Item.Logout(ListCell.Position.SINGLE))
        }
        uiItems.add(Item.Space)
        uiItems.add(Item.Logo)

        _uiItemsFlow.value = uiItems
    }
}