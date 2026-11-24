package com.sushanth.onlysticks.domain.economy

import java.time.LocalDate

enum class ClaimRejection {
    ALREADY_CLAIMED,
    DATE_BEFORE_LAST_CLAIM,
    CLOCK_MOVED_BACK,
}

object ClaimGuard {
    fun check(lastClaimDate: LocalDate?, lastClaimEpochMs: Long?, claimDate: LocalDate, nowEpochMs: Long): ClaimRejection? = when {
        lastClaimDate != null && claimDate == lastClaimDate -> ClaimRejection.ALREADY_CLAIMED
        lastClaimDate != null && claimDate.isBefore(lastClaimDate) -> ClaimRejection.DATE_BEFORE_LAST_CLAIM
        lastClaimEpochMs != null && nowEpochMs < lastClaimEpochMs -> ClaimRejection.CLOCK_MOVED_BACK
        else -> null
    }
}
