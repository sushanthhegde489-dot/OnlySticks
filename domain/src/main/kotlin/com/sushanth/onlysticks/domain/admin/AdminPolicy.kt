// Decides which Google accounts get access to the in-app admin tools.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.admin

/** Pure helpers for the admin allow-list (configured via the ADMIN_EMAILS build property). */
object AdminPolicy {
    /**
     * Splits a comma/semicolon/space separated list into a normalised (trimmed, lower-case) set of emails.
     * Entries without an '@' are dropped.
     */
    fun parse(list: String): Set<String> =
        list.split(',', ';', ' ').map { it.trim().lowercase() }.filter { it.contains('@') }.toSet()

    /** True when [email] is non-null and appears in [admins] (case-insensitive). */
    fun isAdmin(email: String?, admins: Set<String>): Boolean =
        email != null && email.trim().lowercase() in admins
}
