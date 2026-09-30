package com.walkmark.app.presentation.support

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.stringResource
import walkmark.composeapp.generated.resources.Res
import walkmark.composeapp.generated.resources.support_back
import walkmark.composeapp.generated.resources.support_contact_body
import walkmark.composeapp.generated.resources.support_contact_button
import walkmark.composeapp.generated.resources.support_contact_failure
import walkmark.composeapp.generated.resources.support_contact_no_app
import walkmark.composeapp.generated.resources.support_contact_not_configured
import walkmark.composeapp.generated.resources.support_contact_subject
import walkmark.composeapp.generated.resources.support_faq_heading
import walkmark.composeapp.generated.resources.support_privacy_notice
import walkmark.composeapp.generated.resources.support_title

@Composable
fun SupportScreen(
    contactConfig: SupportContactConfig,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contactLauncher: SupportContactLauncher = rememberSupportContactLauncher()
) {
    var contactResult by remember { mutableStateOf<SupportContactResult?>(null) }
    val email = (contactConfig as? SupportContactConfig.Email)?.address
    val subject = stringResource(Res.string.support_contact_subject)
    val body = stringResource(Res.string.support_contact_body)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("support_screen")
            .semantics { contentDescription = "Help and Support Screen" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("support_back_button")
                        .semantics { contentDescription = "Back from Help and Support" }
                ) {
                    Text(stringResource(Res.string.support_back))
                }
                Text(
                    text = stringResource(Res.string.support_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.testTag("support_title")
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.support_privacy_notice),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("support_privacy_notice")
            )

            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.support_faq_heading),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                supportFaqItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("support_faq_${item.id}")
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(item.question), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(item.answer), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val recipient = email ?: return@Button
                    contactResult = null
                    contactLauncher.launch(
                        SupportContactRequest(recipient, subject, body),
                        onResult = { contactResult = it }
                    )
                },
                enabled = email != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("support_contact_button")
                    .semantics { contentDescription = "Contact support by email" }
            ) {
                Text(stringResource(Res.string.support_contact_button))
            }

            when {
                email == null -> Text(
                    stringResource(Res.string.support_contact_not_configured),
                    modifier = Modifier.testTag("support_contact_not_configured")
                )
                contactResult == SupportContactResult.NoCompatibleApp -> Text(
                    stringResource(Res.string.support_contact_no_app),
                    modifier = Modifier.testTag("support_contact_error")
                )
                contactResult == SupportContactResult.Failure -> Text(
                    stringResource(Res.string.support_contact_failure),
                    modifier = Modifier.testTag("support_contact_error")
                )
                else -> Unit
            }
        }
    }
}
