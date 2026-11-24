package com.sushanth.onlysticks.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneId

interface StpSrc {
    suspend fun hasPerm(): Boolean
    suspend fun stpsOn(date: LocalDate, zone: ZoneId): Long
}

class HpConnectStpSrc(private val context: Context) : StpSrc {
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

    override suspend fun stpsOn(date: LocalDate, zone: ZoneId): Long {
        val client = clientOrNull() ?: return 0
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        val result = client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), TimeRangeFilter.between(start, end)))
        return result[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    companion object {
        val READ_STEPS: String = HealthPermission.getReadPermission(StepsRecord::class)

        val PERMISSIONS: Set<String> = setOf(READ_STEPS)

        fun isAvail(context: Context): Boolean =
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }
}
