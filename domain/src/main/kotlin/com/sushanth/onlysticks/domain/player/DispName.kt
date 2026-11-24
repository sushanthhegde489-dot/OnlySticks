package com.sushanth.onlysticks.domain.player

import com.sushanth.onlysticks.domain.config.Cfg

sealed interface DispNameRes {
    data class Valid(val name: String) : DispNameRes
    data object TooShort : DispNameRes
    data object TooLong : DispNameRes
    data object BadCharacters : DispNameRes
}

object DispName {
    private val allowed = Regex("[\\p{L}\\p{N} _.-]+")

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
