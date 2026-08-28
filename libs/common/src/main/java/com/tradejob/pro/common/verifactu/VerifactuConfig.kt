package com.tradejob.pro.common.verifactu

data class VerifactuConfig(
    val enabled: Boolean = false,
    val nif: String = "",
    val businessName: String = "",
    val invoiceSeries: String = "A",
    val nextInvoiceNumber: Int = 1,
    val simpificadoRegime: Boolean = true
)

object VerifactuDefaults {
    const val AEAT_TEST_URL = "https://preprod1.gestiona.com.es"
    const val AEAT_PROD_URL = "https://www1.agenciatributaria.gob.es"
}
