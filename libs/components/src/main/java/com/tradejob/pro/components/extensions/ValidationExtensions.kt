package com.tradejob.pro.components.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tradejob.pro.common.validation.ValidationResult

@Composable
fun ValidationResult.asString(): String? {
    return when (this) {
        is ValidationResult.Error -> stringResource(id = this.messageRes)
        is ValidationResult.Success -> null
    }
}
