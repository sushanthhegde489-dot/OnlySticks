// Seeded, platform-independent random number generator (SplitMix64).
// Both PvP devices use it with the same seed so they compute identical match results.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.rng

/** SplitMix64 RNG. Unlike java.util.Random its output is fully specified, so it is identical on every device. */
class DeterministicRng(seed: Long) {
    private var state: Long = seed

    /** Next 64-bit value (SplitMix64 step: add golden-ratio constant, then mix). */
    fun nextLong(): Long {
        state += -0x61c8864680b583ebL
        var z = state
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
        return z xor (z ushr 31)
    }

    /** Uniform double in [0, 1) using the top 53 bits. */
    fun nextDouble(): Double = (nextLong() ushr 11) * (1.0 / (1L shl 53))

    /** Uniform int in [from, untilExclusive). */
    fun nextInt(from: Int, untilExclusive: Int): Int {
        require(untilExclusive > from) { "empty range $from until $untilExclusive" }
        val bound = (untilExclusive.toLong() - from)
        return (from + Math.floorMod(nextLong(), bound)).toInt()
    }

    /** Uniform int in an inclusive [range]. */
    fun nextInt(range: IntRange): Int = nextInt(range.first, range.last + 1)

    /** True with the given [probability]. */
    fun nextBoolean(probability: Double): Boolean = nextDouble() < probability

    /** Picks an index with probability proportional to its weight. */
    fun weightedIdx(weights: List<Int>): Int {
        val tot = weights.sum()
        require(tot > 0) { "weights must sum to > 0" }
        var roll = nextInt(0, tot)
        weights.forEachIndexed { i, w -> if (roll < w) return i; roll -= w }
        return weights.lastIndex
    }

    companion object {
        /** Folds arbitrary bytes (e.g. a SHA-256 hash) into a 64-bit seed by XOR-ing 8-byte chunks. */
        fun fromBytes(bytes: ByteArray): DeterministicRng {
            require(bytes.isNotEmpty()) { "seed bytes must not be empty" }
            var seed = 0L
            bytes.toList().chunked(8).forEach { chunk ->
                var word = 0L
                for (i in 0 until 8) word = (word shl 8) or ((chunk.getOrElse(i) { 0 }.toLong()) and 0xFF)
                seed = seed xor word
            }
            return DeterministicRng(seed)
        }
    }
}
