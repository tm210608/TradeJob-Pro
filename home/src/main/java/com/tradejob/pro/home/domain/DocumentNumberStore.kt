package com.tradejob.pro.home.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentNumberStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("document_numbers", Context.MODE_PRIVATE)

    fun lastIssued(prefix: String, year: Int): Int =
        prefs.getInt("$prefix-$year", 0)

    fun recordIssued(prefix: String, year: Int, sequence: Int) {
        prefs.edit().putInt("$prefix-$year", sequence).apply()
    }
}
