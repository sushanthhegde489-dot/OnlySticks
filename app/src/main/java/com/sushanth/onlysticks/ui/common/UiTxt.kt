// Resource-based text that ViewModels can create without a Context.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** A string resource id plus format arguments. Arguments may themselves be [UiTxt] (resolved recursively). */
data class UiTxt(@StringRes val id: Int, val args: List<Any> = emptyList())

/** Shorthand for building a [UiTxt]. */
fun uiTxt(@StringRes id: Int, vararg args: Any): UiTxt = UiTxt(id, args.toList())

/** Resolves the text in a composable. */
@Composable
fun UiTxt.asStr(): String {
    val resources = LocalContext.current.resources
    fun resolve(text: UiTxt): String =
        resources.getString(text.id, *text.args.map { if (it is UiTxt) resolve(it) else it }.toTypedArray())
    return resolve(this)
}
