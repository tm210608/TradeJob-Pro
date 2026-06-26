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
    val specialty: String? = null, // Ej: Fontanería, Electricidad
    val firebaseUid: String? = null
)
