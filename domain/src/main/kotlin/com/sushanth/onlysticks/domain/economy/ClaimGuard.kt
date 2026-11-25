// Anti-cheat checks for once-per-day rewards (daily bonus, step rewards).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.economy

import java.time.LocalDate

/** Why a daily claim was refused. */
enum class ClaimRejection {
    ALREADY_CLAIMED,
    DATE_BEFORE_LAST_CLAIM,
    CLOCK_MOVED_BACK,
}

/** Shared guard used by every daily reward. */
object ClaimGuard {
    /**
     * Returns null if the claim is allowed, otherwise the reason it was rejected.
     * Rejects repeat claims for the same day, claims dated before the last one, and device clocks set backwards.
     */
    fun check(lastClaimDate: LocalDate?, lastClaimEpochMs: Long?, claimDate: LocalDate, nowEpochMs: Long): ClaimRejection? = when {
        lastClaimDate != null && claimDate == lastClaimDate -> ClaimRejection.ALREADY_CLAIMED
        lastClaimDate != null && claimDate.isBefore(lastClaimDate) -> ClaimRejection.DATE_BEFORE_LAST_CLAIM
        lastClaimEpochMs != null && nowEpochMs < lastClaimEpochMs -> ClaimRejection.CLOCK_MOVED_BACK
        else -> null
    }
}
