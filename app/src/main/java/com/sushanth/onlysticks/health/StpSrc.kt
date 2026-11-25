// Reads step counts from Health Connect.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneId

/** Source of daily step totals (an interface so tests can fake it). */
interface StpSrc {
    suspend fun hasPerm(): Boolean
    /** Total steps recorded on [date] in time zone [zone]. */
    suspend fun stpsOn(date: LocalDate, zone: ZoneId): Long
}

/** [StpSrc] backed by Health Connect. Returns 0 / no permission when Health Connect isn't installed. */
class HpConnectStpSrc(private val context: Context) : StpSrc {
    /** The Health Connect client, or null if the SDK isn't available on this device. */
    private fun clientOrNull(): HealthConnectClient? =
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }

    override suspend fun hasPerm(): Boolean {
        val client = clientOrNull() ?: return false
        return READ_STEPS in client.permissionController.getGrantedPermissions()
    }

    /** Aggregates step records between local midnight and the next midnight. */
    override suspend fun stpsOn(date: LocalDate, zone: ZoneId): Long {
        val client = clientOrNull() ?: return 0
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        val result = client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), TimeRangeFilter.between(start, end)))
        return result[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    companion object {
        /** The single permission the app asks for: read steps. */
        val READ_STEPS: String = HealthPermission.getReadPermission(StepsRecord::class)

        val PERMISSIONS: Set<String> = setOf(READ_STEPS)

        /** True if Health Connect is installed and usable. */
        fun isAvail(context: Context): Boolean =
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }
}
