package com.sushanth.onlysticks.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

data class UiTxt(@StringRes val id: Int, val args: List<Any> = emptyList())

fun uiTxt(@StringRes id: Int, vararg args: Any): UiTxt = UiTxt(id, args.toList())

@Composable
fun UiTxt.asStr(): String {
    val resources = LocalContext.current.resources
    fun resolve(text: UiTxt): String =
        resources.getString(text.id, *text.args.map { if (it is UiTxt) resolve(it) else it }.toTypedArray())
    return resolve(this)
}
