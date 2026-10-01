package com.walkmark.app.domain.cloud

import kotlinx.serialization.Serializable

/**
 * Remote, provider-assigned identifier for a persisted cloud row.
 *
 * This is provider metadata, not a local identity. [CloudWalk.localId] remains the
 * authoritative local identifier and is never rewritten. The value is null until
 * the provider assigns one on first successful upsert, and Room is not required
 * to store it.
 */
@Serializable
value class CloudEntityId(val value: String)