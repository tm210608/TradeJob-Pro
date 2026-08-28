package com.tradejob.pro.common.verifactu

class VerifactuServiceImpl : VerifactuService {

    override val isEnabled: Boolean = false

    override suspend fun sendInvoice(invoice: VerifactuInvoiceData, config: VerifactuConfig): VerifactuResult {
        if (!isEnabled || !config.enabled) return VerifactuResult.Disabled
        return VerifactuResult.Disabled
    }

    override suspend fun cancelInvoice(invoice: VerifactuInvoiceData, config: VerifactuConfig): VerifactuResult {
        if (!isEnabled || !config.enabled) return VerifactuResult.Disabled
        return VerifactuResult.Disabled
    }
}
