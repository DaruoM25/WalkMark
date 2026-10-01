package com.walkmark.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.promo.JuryPromoManager
import com.walkmark.app.domain.promo.PromoActivationResult
import org.jetbrains.compose.resources.stringResource
import walkmark.composeapp.generated.resources.Res
import walkmark.composeapp.generated.resources.settings_about_app_name
import walkmark.composeapp.generated.resources.settings_about_description
import walkmark.composeapp.generated.resources.settings_about_title
import walkmark.composeapp.generated.resources.settings_back
import walkmark.composeapp.generated.resources.settings_delete_all_button
import walkmark.composeapp.generated.resources.settings_delete_all_cancel
import walkmark.composeapp.generated.resources.settings_delete_all_confirm
import walkmark.composeapp.generated.resources.settings_delete_all_description
import walkmark.composeapp.generated.resources.settings_delete_all_dialog_message
import walkmark.composeapp.generated.resources.settings_delete_all_dialog_title
import walkmark.composeapp.generated.resources.settings_privacy_data_description
import walkmark.composeapp.generated.resources.settings_privacy_data_title
import walkmark.composeapp.generated.resources.settings_support_button
import walkmark.composeapp.generated.resources.settings_support_description
import walkmark.composeapp.generated.resources.settings_support_title
import walkmark.composeapp.generated.resources.settings_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    juryPromoManager: JuryPromoManager? = null,
    onNavigateToAccount: (() -> Unit)? = null,
    onNavigateToSupport: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalWalks by viewModel.totalWalkCount.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var promoInput by remember { mutableStateOf("") }
    var promoFeedback by remember { mutableStateOf<String?>(null) }
    var isPromoError by remember { mutableStateOf(false) }

    val isJuryActive = juryPromoManager?.isJuryAccessActive?.collectAsState()?.value ?: false
    val juryValidUntil = juryPromoManager?.validUntil?.collectAsState()?.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.settings_title),
                        modifier = Modifier
                            .testTag("settings_title")
                            .semantics { contentDescription = "Settings screen title" }
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("settings_back_button")
                            .semantics { contentDescription = "Back to main screen" }
                    ) {
                        Text(stringResource(Res.string.settings_back))
                    }
                }
            )
        },
        modifier = modifier.testTag("settings_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            if (onNavigateToAccount != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settings_account_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "Account", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(text = "Manage your WalkMark account and login state.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToAccount,
                            modifier = Modifier.testTag("settings_account_button")
                        ) {
                            Text("Manage Account")
                        }
                    }
                }
            }

            // Jury Promo Section
            if (juryPromoManager != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settings_jury_promo_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "Demo & Promo Access", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        if (isJuryActive) {
                            Text(
                                text = "Jury access active (Valid until ${juryValidUntil ?: "2026-10-15"})",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag("jury_promo_active_text")
                            )
                        } else {
                            Text(
                                text = "Enter demo promo code for unlimited walk recording.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = promoInput,
                                onValueChange = { promoInput = it; promoFeedback = null },
                                label = { Text("Promo Code") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("promo_code_input")
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    when (val result = juryPromoManager.activate(promoInput)) {
                                        is PromoActivationResult.Success -> {
                                            promoFeedback = "Jury access activated until ${result.validUntil}"
                                            isPromoError = false
                                            promoInput = ""
                                        }
                                        PromoActivationResult.InvalidCode -> {
                                            promoFeedback = "Invalid promo code"
                                            isPromoError = true
                                        }
                                        PromoActivationResult.Expired -> {
                                            promoFeedback = "Promo code expired"
                                            isPromoError = true
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("promo_activate_button")
                            ) {
                                Text("Activate Jury Access")
                            }
                        }

                        if (promoFeedback != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = promoFeedback!!,
                                color = if (isPromoError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.testTag(if (isPromoError) "promo_error_text" else "promo_success_text")
                            )
                        }
                    }
                }
            }

            // Local Data Section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_privacy_data_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_privacy_data_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.settings_privacy_data_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(Res.string.settings_delete_all_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        enabled = totalWalks > 0,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .testTag("settings_delete_all_button")
                            .semantics { contentDescription = "Delete all walks" }
                    ) {
                        Text(stringResource(Res.string.settings_delete_all_button))
                    }
                }
            }

            // Support Section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_support_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_support_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.settings_support_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToSupport,
                        modifier = Modifier
                            .testTag("settings_support_button")
                            .semantics { contentDescription = "Open Support and Diagnostics" }
                    ) {
                        Text(stringResource(Res.string.settings_support_button))
                    }
                }
            }

            // About Section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_about_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_about_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.settings_about_app_name),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("settings_about_app_name")
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.settings_about_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("settings_about_description")
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(Res.string.settings_delete_all_dialog_title),
                    modifier = Modifier.testTag("delete_all_dialog_title")
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.settings_delete_all_dialog_message),
                    modifier = Modifier.testTag("delete_all_dialog_message")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteAllWalks()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .testTag("delete_all_confirm_button")
                        .semantics { contentDescription = "Confirm Delete All Walks" }
                ) {
                    Text(stringResource(Res.string.settings_delete_all_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    modifier = Modifier
                        .testTag("delete_all_cancel_button")
                        .semantics { contentDescription = "Cancel Delete All Walks" }
                ) {
                    Text(stringResource(Res.string.settings_delete_all_cancel))
                }
            },
            modifier = Modifier.testTag("delete_all_confirmation_dialog")
        )
    }
}
