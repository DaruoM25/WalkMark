package com.walkmark.app.presentation.support

import org.jetbrains.compose.resources.StringResource
import walkmark.composeapp.generated.resources.Res
import walkmark.composeapp.generated.resources.faq_contact_answer
import walkmark.composeapp.generated.resources.faq_contact_question
import walkmark.composeapp.generated.resources.faq_getting_started_answer
import walkmark.composeapp.generated.resources.faq_getting_started_question
import walkmark.composeapp.generated.resources.faq_gps_answer
import walkmark.composeapp.generated.resources.faq_gps_question
import walkmark.composeapp.generated.resources.faq_location_permission_answer
import walkmark.composeapp.generated.resources.faq_location_permission_question
import walkmark.composeapp.generated.resources.faq_privacy_answer
import walkmark.composeapp.generated.resources.faq_privacy_question
import walkmark.composeapp.generated.resources.faq_start_stop_answer
import walkmark.composeapp.generated.resources.faq_start_stop_question

sealed interface SupportContactConfig {
    data object NotConfigured : SupportContactConfig

    data class Email(val address: String) : SupportContactConfig {
        init {
            require(isValidSupportEmail(address)) { "Invalid support email configuration" }
        }
    }
}

fun isValidSupportEmail(address: String): Boolean {
    if (address != address.trim() || address.any(Char::isWhitespace)) return false
    val atIndex = address.indexOf('@')
    return atIndex > 0 && atIndex == address.lastIndexOf('@') && atIndex < address.lastIndex
}

data class SupportContactRequest(
    val recipient: String,
    val subject: String,
    val body: String
)

enum class SupportContactResult {
    Success,
    NoCompatibleApp,
    Failure
}

data class SupportFaqItem(
    val id: String,
    val question: StringResource,
    val answer: StringResource
)

val supportFaqItems = listOf(
    SupportFaqItem("getting_started", Res.string.faq_getting_started_question, Res.string.faq_getting_started_answer),
    SupportFaqItem("start_stop", Res.string.faq_start_stop_question, Res.string.faq_start_stop_answer),
    SupportFaqItem("location_permission", Res.string.faq_location_permission_question, Res.string.faq_location_permission_answer),
    SupportFaqItem("gps_tracking", Res.string.faq_gps_question, Res.string.faq_gps_answer),
    SupportFaqItem("privacy", Res.string.faq_privacy_question, Res.string.faq_privacy_answer),
    SupportFaqItem("contact", Res.string.faq_contact_question, Res.string.faq_contact_answer)
)
