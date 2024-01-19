package com.tonapps.singer.screen.root.action

sealed class RootAction {
    data class KeyDetails(val id: Long): RootAction()
    data class RequestBodySign(val id: Long, val body: String, val qr: Boolean): RootAction()
    data class ResponseBoc(val boc: String): RootAction()
}