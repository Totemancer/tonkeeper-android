package com.tonapps.extensions

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.core.content.edit
import com.tonapps.base64.decodeBase64
import com.tonapps.base64.encodeBase64

fun SharedPreferences.getByteArray(key: String): ByteArray? {
    val value = getString(key, null)?.trim() ?: return null
    val bytes = value.decodeBase64()
    if (bytes.isEmpty()) {
        return null
    }
    return bytes
}

/*
fun SharedPreferences.getByteArray(key: String): ByteArray? {
    val value = run {
        val value = getString(key, null)
        if (value.isNullOrBlank()) {
            return byteArrayOf(0)
        }
        base64(value) ?: byteArrayOf(0)
    }

    if (value.isZero()) {
        value.clear()
        return null
    }
    return value
}

fun SharedPreferences.Editor.putByteArray(key: String, value: ByteArray): SharedPreferences.Editor {
    if (value.isZero()) {
        remove(key)
        return this
    } else {
        val string = base64(value)
        if (string == null) {
            remove(key)
        } else {
            putString(key, string)
        }
    }
    return this
}

 */

fun SharedPreferences.putByteArray(key: String, value: ByteArray?) {
    if (value == null) {
        remove(key)
    } else {
        edit {
            putByteArray(key, value)
        }
    }
}

fun SharedPreferences.Editor.putByteArray(key: String, value: ByteArray?): SharedPreferences.Editor {
    if (value == null) {
        remove(key)
    } else {
        putString(key, value.encodeBase64())
    }
    return this
}

fun SharedPreferences.getIntArray(key: String): IntArray? {
    if (!contains(key)) {
        return null
    }
    val value = getString(key, null) ?: return null
    return value.split(",").mapNotNull { it.toIntOrNull() }.toIntArray()
}

inline fun <reified R: Parcelable> SharedPreferences.getParcelable(key: String): R? {
    val bytes = getByteArray(key) ?: return null
    return bytes.toParcel<R>()
}

fun SharedPreferences.putParcelable(key: String, value: Parcelable?) {
    if (value == null) {
        remove(key)
    } else {
        edit {
            putByteArray(key, value.toByteArray())
        }
    }
}

fun SharedPreferences.putIntArray(key: String, value: IntArray) {
    edit {
        putString(key, value.joinToString(","))
    }
}

fun SharedPreferences.remove(key: String) {
    edit {
        remove(key)
    }
}

fun SharedPreferences.clear() {
    edit {
        clear()
    }
}

fun SharedPreferences.putString(key: String, value: String?) {
    if (value.isNullOrBlank()) {
        remove(key)
    } else {
        edit {
            putString(key, value)
        }
    }
}

fun SharedPreferences.putLong(key: String, value: Long) {
    edit {
        putLong(key, value)
    }
}

fun SharedPreferences.putInt(key: String, value: Int) {
    edit {
        putInt(key, value)
    }
}

fun SharedPreferences.getStringArray(key: String): Array<String> {
    val string = getString(key, null) ?: return emptyArray()
    return string.split(",").toTypedArray()
}


fun SharedPreferences.putStringArray(key: String, value: Array<String>) {
    putString(key, value.joinToString(","))
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) {
    edit {
        putBoolean(key, value)
    }
}

inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, default: T): T {
    val value = this.getInt(key, -1)
    return if (value >= 0) {
        enumValues<T>()[value]
    } else {
        default
    }
}

fun <T : Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: T?) = apply {
    this.putInt(key, value?.ordinal ?: -1)
}

fun SharedPreferences.string(key: String): String? {
    val value = this.getString(key, null)
    if (value.isNullOrEmpty()) {
        return null
    }
    return value
}

fun SharedPreferences.putStringIfNotExists(key: String, value: String) {
    if (!contains(key)) {
        putString(key, value)
    }
}

fun SharedPreferences.string(key: String, value: String?) {
    edit {
        if (value.isNullOrBlank()) {
            remove(key)
        } else {
            putString(key, value)
        }
    }
}