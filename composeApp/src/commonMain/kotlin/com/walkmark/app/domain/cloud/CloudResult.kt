package com.walkmark.app.domain.cloud

/**
 * Provider-neutral outcome of a cloud storage operation.
 *
 * Ports never throw for remote failures; they return [Failure] so that a caller
 * can continue using local data when the network or the service is unavailable.
 */
sealed interface CloudResult<out T> {

    data class Success<T>(val value: T) : CloudResult<T>

    data class Failure(val failure: CloudFailure) : CloudResult<Nothing>
}

/**
 * Closed taxonomy of cloud storage failures.
 *
 * Carries no [Throwable] and no free-text provider or server message: an
 * implementation must translate transport and server errors into one of these
 * cases before the value reaches the domain.
 *
 * This taxonomy describes the outcome of a single storage call. Orchestration of
 * a whole sync run, including translating these cases into a sync-level outcome,
 * belongs to the sync layer and is not modelled here.
 */
sealed interface CloudFailure {

    data object NotAuthenticated : CloudFailure

    data object NetworkUnavailable : CloudFailure

    data class RateLimited(val retryAfterEpochMs: Long? = null) : CloudFailure

    data object ServiceUnavailable : CloudFailure

    data object Rejected : CloudFailure

    data object Unknown : CloudFailure
}