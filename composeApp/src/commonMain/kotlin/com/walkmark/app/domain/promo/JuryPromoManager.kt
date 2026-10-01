package com.walkmark.app.domain.promo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PromoActivationResult {
    data class Success(val validUntil: String) : PromoActivationResult
    data object InvalidCode : PromoActivationResult
    data object Expired : PromoActivationResult
}

class JuryPromoManager(
    private val currentDate: String = "2026-10-01"
) {
    companion object {
        const val JURY_PROMO_CODE = "WALKMARK-JURY-2026"
        const val VALID_FROM = "2026-10-01"
        const val VALID_UNTIL = "2026-10-15"
    }

    private val _isJuryAccessActive = MutableStateFlow(false)
    val isJuryAccessActive: StateFlow<Boolean> = _isJuryAccessActive.asStateFlow()

    private val _validUntil = MutableStateFlow<String?>(null)
    val validUntil: StateFlow<String?> = _validUntil.asStateFlow()

    fun activate(code: String, date: String = currentDate): PromoActivationResult {
        val trimmed = code.trim().uppercase()
        if (trimmed != JURY_PROMO_CODE) {
            return PromoActivationResult.InvalidCode
        }

        if (date < VALID_FROM || date > VALID_UNTIL) {
            return PromoActivationResult.Expired
        }

        _isJuryAccessActive.value = true
        _validUntil.value = VALID_UNTIL
        return PromoActivationResult.Success(VALID_UNTIL)
    }

    fun deactivate() {
        _isJuryAccessActive.value = false
        _validUntil.value = null
    }
}
