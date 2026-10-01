package com.walkmark.app.presentation.paywall

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.promo.JuryPromoManager
import com.walkmark.app.domain.promo.PromoActivationResult

@Composable
fun HardPaywallSheet(
    state: HardPaywallUiState,
    juryPromoManager: JuryPromoManager? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    var promoInput by remember { mutableStateOf("") }
    var promoError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        modifier = modifier.testTag("paywall_sheet"),
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Column {
                Text(
                    text = "WalkMark Premium",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .testTag("paywall_title")
                        .semantics { contentDescription = "WalkMark Premium" }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Unlimited walks",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Continue tracking without limits",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("paywall_body")
                )

                if (state.showsStorePrices) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paywall_prices")
                    ) {
                        state.products.forEach { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("paywall_price_${product.id}"),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${product.title} (${product.period})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = product.priceFormatted,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.testTag("paywall_price_value_${product.id}")
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Purchases are not available in this build.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .testTag("paywall_provider_unavailable")
                            .semantics { contentDescription = "Purchases are not available in this build" }
                    )
                }

                if (juryPromoManager != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Enter jury access code",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = promoInput,
                        onValueChange = { promoInput = it; promoError = null },
                        label = { Text("Enter WALKMARK-JURY-2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("paywall_promo_input")
                    )
                    if (promoError != null) {
                        Text(
                            text = promoError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("paywall_promo_error")
                        )
                    }
                    Button(
                        onClick = {
                            when (juryPromoManager.activate(promoInput)) {
                                is PromoActivationResult.Success -> {
                                    onDismiss()
                                }
                                PromoActivationResult.InvalidCode -> {
                                    promoError = "Invalid promo code"
                                }
                                PromoActivationResult.Expired -> {
                                    promoError = "Promo code expired"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paywall_promo_activate_button"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Apply Promo Code")
                    }
                }

                state.unavailableMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("paywall_provider_status")
                    )
                }
            }
        },
        confirmButton = {
            if (state.hasEnabledPurchaseControl()) {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.testTag("paywall_purchase_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Upgrade to Premium", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.testTag("paywall_purchase_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Purchases unavailable in this build")
                }
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                TextButton(
                    onClick = { },
                    enabled = state.hasEnabledRestoreControl(),
                    modifier = Modifier
                        .testTag("paywall_restore_button")
                        .semantics { contentDescription = "Restore purchases" }
                ) {
                    Text(
                        text = if (state.hasEnabledRestoreControl()) {
                            "Restore purchases"
                        } else {
                            "Restore purchases (unavailable)"
                        }
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .testTag("paywall_close_button")
                        .semantics { contentDescription = "Close" }
                ) {
                    Text("Close")
                }
            }
        }
    )
}
