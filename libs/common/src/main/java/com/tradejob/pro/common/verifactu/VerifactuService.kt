package com.tradejob.pro.common.verifactu

data class VerifactuInvoiceData(
    val number: String,
    val total: Double,
    val vatRate: Double,
    val issuedAt: Long,
    val clientId: Long
)

interface VerifactuService {
    val isEnabled: Boolean
    suspend fun sendInvoice(invoice: VerifactuInvoiceData, config: VerifactuConfig): VerifactuResult
    suspend fun cancelInvoice(invoice: VerifactuInvoiceData, config: VerifactuConfig): VerifactuResult
}

sealed class VerifactuResult {
    data class Success(val recordId: String, val qrUrl: String) : VerifactuResult()
    data class Error(val code: String, val message: String) : VerifactuResult()
    data object Disabled : VerifactuResult()
}
