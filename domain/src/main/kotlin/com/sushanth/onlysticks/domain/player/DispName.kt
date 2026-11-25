// Validation for player display names.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.player

import com.sushanth.onlysticks.domain.config.Cfg

/** Result of validating a display name. */
sealed interface DispNameRes {
    data class Valid(val name: String) : DispNameRes
    data object TooShort : DispNameRes
    data object TooLong : DispNameRes
    data object BadCharacters : DispNameRes
}

/** Display name rules: 3-16 characters of letters, digits, spaces, '_', '.' or '-'. */
object DispName {
    private val allowed = Regex("[\\p{L}\\p{N} _.-]+")

    /** Trims and collapses whitespace, then checks length and allowed characters. */
    fun check(raw: String): DispNameRes {
        val name = raw.trim().replace(Regex("\\s+"), " ")
        return when {
            name.length < Cfg.Prof.NAME_MIN -> DispNameRes.TooShort
            name.length > Cfg.Prof.NAME_MAX -> DispNameRes.TooLong
            !allowed.matches(name) -> DispNameRes.BadCharacters
            else -> DispNameRes.Valid(name)
        }
    }
}
