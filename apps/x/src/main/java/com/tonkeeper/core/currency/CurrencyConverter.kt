package com.tonkeeper.core.currency

import android.util.Log
import com.tonkeeper.App
import com.tonkeeper.api.to
import com.tonkeeper.core.Coin
import io.tonapi.models.TokenRates
import org.ton.block.AddrStd
import ton.SupportedCurrency
import ton.SupportedTokens
import ton.wallet.Wallet

fun Wallet.currency(fromCurrency: String) = from(fromCurrency, accountId, testnet)

fun Wallet.currency(fromCurrency: SupportedTokens) = from(fromCurrency, accountId, testnet)

fun Wallet.ton(value: Float) = currency(SupportedTokens.TON).value(value)

fun Wallet.ton(value: Long) = currency(SupportedTokens.TON).value(value)

fun from(
    fromCurrency: String,
    accountId: String,
    testnet: Boolean,
): CurrencyConverter {
    return CurrencyConverter(fromCurrency, accountId, testnet)
}

fun from(
    fromCurrency: SupportedTokens,
    accountId: String,
    testnet: Boolean,
): CurrencyConverter {
    return from(fromCurrency.code, accountId, testnet)
}

class CurrencyConverter(
    private val fromCurrency: String,
    private val accountId: String,
    private val testnet: Boolean,
) {

    private var value = 0f

    fun value(value: Float) = apply {
        this.value = value
    }

    fun value(value: Long) = apply {
        value(Coin.toCoins(value))
    }

    suspend fun to(to: SupportedCurrency = App.settings.currency): Float {
        return to(to.code)
    }

    suspend fun to(to: String): Float {
        if (fromCurrency == to) {
            return value
        }
        if (0f >= value) {
            return 0f
        }

        return try {
            val rates = CurrencyManager.getInstance().get(accountId, testnet) ?: throw Exception("No rates for account $accountId")
            val token = rates[fromCurrency] ?: throw Exception("No rates for $fromCurrency")
            token.to(to, value)
        } catch (e: Throwable) {
            Log.d("CurrencyConverterLog", "Error converting currenc", e)
            0f
        }
    }
}