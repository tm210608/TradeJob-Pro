package com.tradejob.pro.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val specialty: String? = null,
    val firebaseUid: String? = null,
    val nif: String? = null,
    val businessName: String? = null,
    val businessAddress: String? = null,
    val businessCity: String? = null,
    val businessPhone: String? = null,
    val irpfRate: Double? = null,
    val verifactuEnabled: Boolean = false,
    val invoiceSeries: String? = null,
    val invoiceNumber: Int = 0
)
