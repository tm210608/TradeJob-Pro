package com.tradejob.pro.home.ui.jobs

enum class JobStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Pendiente"),
    IN_PROGRESS("IN_PROGRESS", "En curso"),
    COMPLETED("COMPLETED", "Completado"),
    INVOICED("INVOICED", "Facturado");

    companion object {
        fun fromValue(value: String): JobStatus = entries.find { it.value == value } ?: PENDING
    }
}

enum class JobPriority(val value: String, val displayName: String) {
    LOW("LOW", "Baja"),
    MEDIUM("MEDIUM", "Media"),
    HIGH("HIGH", "Alta"),
    URGENT("URGENT", "Urgente");

    companion object {
        fun fromValue(value: String): JobPriority = entries.find { it.value == value } ?: MEDIUM
    }
}
