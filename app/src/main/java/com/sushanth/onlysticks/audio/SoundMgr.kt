package com.sushanth.onlysticks.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.sushanth.onlysticks.R
import kotlin.random.Random

enum class SoundFx(@RawRes val resId: Int) {
    TAP(R.raw.sfx_tap),
    HIT(R.raw.sfx_hit),
    PARRY(R.raw.sfx_parry),
    CRIT(R.raw.sfx_crit),
    WHOOSH(R.raw.sfx_whoosh),
    REST(R.raw.sfx_rest),
    BREAK(R.raw.sfx_break),
    VICTORY(R.raw.sfx_victory),
    DEFEAT(R.raw.sfx_defeat),
    PACK_OPEN(R.raw.sfx_pack_open),
}

class SoundMgr private constructor(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIds = SoundFx.entries.associateWith { pool.load(context, it.resId, 1) }

    fun play(fx: SoundFx, volume: Float = 0.85f, rate: Float = 1f) {
        val id = soundIds.getValue(fx)
        val pitch = (rate * (0.96f + Random.nextFloat() * 0.08f)).coerceIn(0.5f, 2f)
        pool.play(id, volume, volume, 1, 0, pitch)
    }

    companion object {
        private var instance: SoundMgr? = null

        fun get(context: Context): SoundMgr =
            instance ?: SoundMgr(context.applicationContext).also { instance = it }
    }
}
