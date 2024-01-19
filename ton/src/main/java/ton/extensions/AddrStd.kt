package ton.extensions

import org.ton.block.AddrStd

fun String.toUserFriendly(
    wallet: Boolean = true,
    testnet: Boolean
): String {
    return try {
        val addr = AddrStd(this)
        if (wallet) {
            addr.toWalletAddress(testnet)
        } else {
            addr.toString(userFriendly = true)
        }
    } catch (e: Exception) {
        this
    }
}

fun AddrStd.toWalletAddress(testnet: Boolean): String {
    return toString(
        userFriendly = true,
        bounceable = false,
        testOnly = testnet,
    )
}

fun String.toRawAddress(): String {
    return try {
        AddrStd(this).toString(userFriendly = false).lowercase()
    } catch (e: Exception) {
        this
    }
}