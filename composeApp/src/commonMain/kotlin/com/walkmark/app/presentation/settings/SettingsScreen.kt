package com.walkmark.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import walkmark.composeapp.generated.resources.settings_delete_all_cancel
import walkmark.composeapp.generated.resources.settings_delete_all_confirm
import walkmark.composeapp.generated.resources.settings_delete_all_deleting
import walkmark.composeapp.generated.resources.settings_delete_all_dialog_message
import walkmark.composeapp.generated.resources.settings_delete_all_dialog_title
import walkmark.composeapp.generated.resources.settings_delete_all_no_walks
import walkmark.composeapp.generated.resources.settings_delete_all_partial_failure
import walkmark.composeapp.generated.resources.settings_delete_all_success
import walkmark.composeapp.generated.resources.settings_delete_all_walks_button
import walkmark.composeapp.generated.resources.settings_help_support_description
import walkmark.composeapp.generated.resources.settings_help_support_title
import walkmark.composeapp.generated.resources.settings_privacy_data_description
import walkmark.composeapp.generated.resources.settings_privacy_data_title
import walkmark.composeapp.generated.resources.settings_title
import walkmark.composeapp.generated.resources.support_entry

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    juryPromoManager: JuryPromoManager? = null,
    onNavigateToAccount: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bulkDeleteState by viewModel.bulkDeleteState.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var promoInput by remember { mutableStateOf("") }
    var promoFeedback by remember { mutableStateOf<String?>(null) }
    var isPromoError by remember { mutableStateOf(false) }

    val isPromoActive by (juryPromoManager?.isJuryAccessActive?.collectAsState() ?: remember { mutableStateOf(false) })
    val promoValidUntil by (juryPromoManager?.validUntil?.collectAsState() ?: remember { mutableStateOf<String?>(null) })

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("settings_screen")
            .semantics { contentDescription = "Settings Screen" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("settings_back_button")
                        .semantics { contentDescription = "Back from Settings" }
                ) {
                    Text(stringResource(Res.string.settings_back))
                }
                Text(
                    text = stringResource(Res.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.testTag("settings_title")
                )
            }

            Spacer(Modifier.height(16.dp))

            // Section 0: Account
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_section_account")
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Manage your authentication status, log in or register.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToAccount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_account_button")
                            .semantics { contentDescription = "Manage Account" }
                    ) {
                        Text("Manage Account")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Section 0.5: Jury Promo Access
            if (juryPromoManager != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_section_promo")
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Jury Promo Access",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))

                        if (isPromoActive) {
                            Text(
                                text = "Jury access active (valid through " + (promoValidUntil ?: JuryPromoManager.VALID_UNTIL) + ")",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("jury_promo_active_badge")
                            )
                        } else {
                            Text(
                                text = "Enter a jury promo code for temporary full premium access.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = promoInput,
                                onValueChange = { promoInput = it },
                                label = { Text("Promo Code") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("promo_code_input")
                            )

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val result = juryPromoManager.activate(promoInput)
                                    when (result) {
                                        is PromoActivationResult.Success -> {
                                            promoFeedback = "Jury access active through " + result.validUntil
                                            isPromoError = false
                                        }
                                        PromoActivationResult.Expired -> {
                                            promoFeedback = "Promo code has expired"
                                            isPromoError = true
                                        }
                                        PromoActivationResult.InvalidCode -> {
                                            promoFeedback = "Invalid promo code"
                                            isPromoError = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("activate_promo_button")
                            ) {
                                Text("Activate")
                            }

                            promoFeedback?.let { feedback ->
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = feedback,
                                    color = if (isPromoError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.testTag("promo_feedback_text")
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Section 1: Privacy & Local Data
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_section_privacy")
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_privacy_data_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.settings_privacy_data_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("settings_privacy_notice")
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        enabled = bulkDeleteState !is BulkDeleteState.InProgress,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_delete_all_walks_button")
                            .semantics { contentDescription = "Delete all local walks" }
                    ) {
                        Text(stringResource(Res.string.settings_delete_all_walks_button))
                    }

                    when (val state = bulkDeleteState) {
                        is BulkDeleteState.InProgress -> {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("settings_delete_status")
                            ) {
                                CircularProgressIndicator(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(Res.string.settings_delete_all_deleting),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        is BulkDeleteState.Success -> {
                            Spacer(Modifier.height(8.dp))
                            val message = if (state.deletedCount == 0) {
                                stringResource(Res.string.settings_delete_all_no_walks)
                            } else {
                                stringResource(Res.string.settings_delete_all_success)
                            }
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("settings_delete_status")
                            )
                        }
                        is BulkDeleteState.PartialSuccess -> {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.settings_delete_all_partial_failure),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.testTag("settings_delete_status")
                            )
                        }
                        is BulkDeleteState.Error -> {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.testTag("settings_delete_status")
                            )
                        }
                        is BulkDeleteState.Idle -> Unit
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Section 2: Help & Support
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_section_support")
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_help_support_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.settings_help_support_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToSupport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_support_button")
                            .semantics { contentDescription = "Open Help and Support" }
                    ) {
                        Text(stringResource(Res.string.support_entry))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Section 3: About
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_section_about")
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
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
