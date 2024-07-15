/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.models

import io.tonapi.models.GaslessEstimateRequestMessagesInner

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param walletAddress 
 * @param walletPublicKey 
 * @param messages 
 */


data class GaslessEstimateRequest (

    @Json(name = "wallet_address")
    val walletAddress: kotlin.String,

    @Json(name = "wallet_public_key")
    val walletPublicKey: kotlin.String,

    @Json(name = "messages")
    val messages: kotlin.collections.List<GaslessEstimateRequestMessagesInner>

)

