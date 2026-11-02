package com.tradejob.pro.home.ui.quotes

import com.tradejob.pro.home.R

enum class QuoteStatus(val value: String, val displayRes: Int) {
    DRAFT("DRAFT", R.string.quote_status_draft),
    SENT("SENT", R.string.quote_status_sent),
    ACCEPTED("ACCEPTED", R.string.quote_status_accepted),
    REJECTED("REJECTED", R.string.quote_status_rejected);

    companion object {
        fun fromValue(value: String): QuoteStatus = entries.find { it.value == value } ?: DRAFT
    }
}
