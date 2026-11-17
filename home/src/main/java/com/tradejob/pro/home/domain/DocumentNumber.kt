package com.tradejob.pro.home.domain

object DocumentNumber {

    fun format(prefix: String, year: Int, sequence: Int): String =
        "$prefix-$year-${sequence.toString().padStart(4, '0')}"

    fun likePattern(prefix: String, year: Int): String = "$prefix-$year-%"

    fun parseSequence(number: String, prefix: String, year: Int): Int? {
        val expectedPrefix = "$prefix-$year-"
        if (!number.startsWith(expectedPrefix)) return null
        return number.removePrefix(expectedPrefix).toIntOrNull()
    }

    fun nextSequence(prefix: String, year: Int, existingNumbers: List<String>, lastIssued: Int = 0): Int {
        val maxExisting = existingNumbers.mapNotNull { parseSequence(it, prefix, year) }.maxOrNull() ?: 0
        return maxOf(maxExisting, lastIssued) + 1
    }

    fun next(prefix: String, year: Int, existingNumbers: List<String>, lastIssued: Int = 0): String =
        format(prefix, year, nextSequence(prefix, year, existingNumbers, lastIssued))
}
