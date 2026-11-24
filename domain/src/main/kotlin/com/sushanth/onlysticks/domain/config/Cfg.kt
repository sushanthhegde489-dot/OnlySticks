package com.sushanth.onlysticks.domain.config

import com.sushanth.onlysticks.domain.model.Rarity
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

object Cfg {
    const val PLAYER_MAX_HP = 1000
    const val STARTING_STAMINA = 100
    const val MAX_STAMINA = 100
    const val BASE_PLAYER_SPEED = 50
    const val DECK_SLOTS = 7
    const val TRAINER_SLOTS = 1
    const val MIN_WAGER_CARDS = 1
    const val MAX_WAGER_CARDS = 3
    const val TURN_TIMER_MS = 7_000L
    const val DURABILITY_FLOOR = 25
    const val PACK_SIZE = 5
    const val WEAR_PER_ATTACK = 10
    const val WEAR_PARRY_SUCCESS = 5
    const val PASSIVE_STAMINA_REGEN = 5
    const val FATIGUE_THRESHOLD = 20
    const val FATIGUE_SPEED_PENALTY = 10
    const val DAMAGE_STAT_DIVISOR = 250.0
    const val PLAYER_SPEED_DAMAGE_DIVISOR = 2.0
    const val CRIT_CHANCE = 0.10
    const val CRIT_MULTIPLIER = 1.5
    const val EXHAUSTION_DAMAGE_MULT = 0.2
    const val UNARMED_DAMAGE_MULT = 0.5
    const val REST_RECOVERY = 30
    const val AUTO_SKIP_RECOVERY = 10
    const val PARRY_CLINK_STAMINA = 10
    const val PARRY_WHIFF_STAMINA = 35
    const val FIRE_BURN_FRACTION = 0.5
    const val FIRE_RECOIL = 40
    const val BURNING_DAMAGE_MULT = 1.2
    const val WET_DAMAGE_MULT = 0.9
    const val WET_SPEED_PENALTY = 20
    const val WET_ROUNDS = 2
    const val STAT_CAP_FACTOR = 1.15
    const val MIN_STAT = 1
    const val TUNING_MAJOR = 0.15
    const val TUNING_MINOR = 0.10
    object Appr {
        fun tierMult(rarity: Rarity): Double = when (rarity) {
            Rarity.COMMON -> 1.0
            Rarity.UNCOMMON -> 1.5
            Rarity.RARE -> 2.25
            Rarity.SUPER_RARE -> 3.5
            Rarity.ULTRA_RARE -> 5.0
        }
        const val LIGHTER_VALUE = 120
        const val WATER_BOTTLE_VALUE = 80
        const val TRAINER_BASE_VALUE = 300
        const val TRAINER_VALUE_PER_RANK = 100
    }
    object Trnr {
        val RANK_THRESHOLDS = listOf(0, 100, 300, 600, 1000, 1500)
        const val MAX_RANK = 5
        const val MAX_HP_PER_RANK = 20
        const val REST_BONUS_PER_RANK = 2
        const val DEFENSE_PCT_PER_RANK = 0.02
        val DECK_SLOT_RANKS = listOf(2, 4)
        const val SPEED_PENALTY_PER_EXTRA_SLOT = 5
        const val WIN_AFFINITY = 50
        const val FULL_XP_MIN_BOT_TIER = 3
        const val EASY_BOT_XP_FACTOR = 0.4
        const val STEPS_PER_AFFINITY = 500
        const val DAILY_STEP_PROGRESSION_CAP = 15_000
    }
    object Economy {
        const val STARTING_CURRENCY = 200L
        const val STEP_CURRENCY_DAILY_CAP = 10_000
        const val DAILY_LOGIN = 50
        val DAILY_STREAK_REWARDS = listOf(50, 60, 70, 80, 100, 120, 200)
        const val PVE_WIN_PER_TIER = 25
        const val STAKE_TOLERANCE = 0.15
    }
    object Bots {
        const val TIERS = 5
        val RANDOM_MOVE_CHANCE = listOf(0.50, 0.35, 0.20, 0.10, 0.05)
        val RARITY_WEIGHTS = listOf(
            listOf(70, 25, 5, 0, 0),
            listOf(45, 35, 15, 5, 0),
            listOf(25, 35, 25, 12, 3),
            listOf(10, 25, 35, 22, 8),
            listOf(5, 15, 30, 30, 20),
        )
        const val POWERUPS_MIN_TIER = 3
        const val DELAY_MIN_MS = 1_500L
        const val DELAY_MAX_MS = 5_500L
        const val TUNING_FLAG_CHANCE = 0.25
        val POWERUP_COUNT = 1..2
        const val LOW_STAMINA = 25
        val MOVE_MIX_WEIGHTS = listOf(30, 35, 20, 15)
    }
    object Store {
        val REFRESH_INTERVAL = 7.days
        val AVAILABILITY_WINDOW = 24.hours
        const val SLOTS = 4
        val RARITY_WEIGHTS = listOf(0, 40, 38, 17, 5)
        const val TUNING_FLAG_CHANCE = 0.25
        const val PRICE_FACTOR = 1.2
        const val PRICE_ROUNDING = 10L
    }
    object Packs {
        const val COMMON_COST = 100L
        const val RARE_COST = 300L
        const val ULTRA_COST = 900L
        val COMMON_WEIGHTS = listOf(700, 250, 40, 9, 1)
        val RARE_WEIGHTS = listOf(300, 400, 230, 60, 10)
        val ULTRA_WEIGHTS = listOf(0, 250, 450, 250, 50)
        const val MAX_TRAINERS = 1
        const val MAX_POWER_UPS = 2
        const val TRAINER_CHANCE = 0.10
        val POWER_UP_CHANCES = listOf(0.70, 0.20)
    }
    object Prof {
        const val NAME_MIN = 3
        const val NAME_MAX = 16
    }
    object Pvp {
        const val PORT = 5000
        const val CONNECT_TIMEOUT_MS = 8_000
        const val ROUND_TIMEOUT_MS = 30_000L
        const val SERVICE_TYPE = "_onlysticks._tcp."
    }
}
