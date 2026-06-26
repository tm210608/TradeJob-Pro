package com.tradejob.pro.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

sealed class MitoButtonSheet {
    abstract val title: String
    abstract val message: String
    abstract val onDismiss: () -> Unit
    
    data class InfoMitoButtonSheet(
        override val title: String,
        override val message: String,
        override val onDismiss: () -> Unit
    ) : MitoButtonSheet()
    
    data class ConfirmMitoButtonSheet(
        override val title: String,
        override val message: String,
        override val onDismiss: () -> Unit,
        val onConfirm: () -> Unit,
        val confirmText: String = "Confirm",
        val dismissText: String = "Cancel"
    ) : MitoButtonSheet()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MitoBottomSheet(
    mitoButtonSheet: MitoButtonSheet
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = mitoButtonSheet.onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mitoButtonSheet.title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = mitoButtonSheet.message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            when (mitoButtonSheet) {
                is MitoButtonSheet.InfoMitoButtonSheet -> {
                    Button(
                        onClick = mitoButtonSheet.onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("OK")
                    }
                }
                is MitoButtonSheet.ConfirmMitoButtonSheet -> {
                    Button(
                        onClick = {
                            mitoButtonSheet.onConfirm()
                            mitoButtonSheet.onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(mitoButtonSheet.confirmText)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = mitoButtonSheet.onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(mitoButtonSheet.dismissText)
                    }
                }
            }
        }
    }
}
