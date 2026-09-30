package com.walkmark.app.domain.monetization

enum class SubscriptionStatus {
    Unknown,
    Free,
    Entitled,
    Error
}

data class SubscriptionState(
    val status: SubscriptionStatus,
    val isEntitled: Boolean,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        fun unknown(isLoading: Boolean = true): SubscriptionState = SubscriptionState(
            status = SubscriptionStatus.Unknown,
            isEntitled = false,
            isLoading = isLoading
        )

        fun free(): SubscriptionState = SubscriptionState(
            status = SubscriptionStatus.Free,
            isEntitled = false
        )

        fun entitled(): SubscriptionState = SubscriptionState(
            status = SubscriptionStatus.Entitled,
            isEntitled = true
        )

        fun error(message: String? = null): SubscriptionState = SubscriptionState(
            status = SubscriptionStatus.Error,
            isEntitled = false,
            errorMessage = message
        )
    }
}
