package com.sushanth.onlysticks.domain.admin

object AdminPolicy {
    fun parse(list: String): Set<String> =
        list.split(',', ';', ' ').map { it.trim().lowercase() }.filter { it.contains('@') }.toSet()

    fun isAdmin(email: String?, admins: Set<String>): Boolean =
        email != null && email.trim().lowercase() in admins
}
