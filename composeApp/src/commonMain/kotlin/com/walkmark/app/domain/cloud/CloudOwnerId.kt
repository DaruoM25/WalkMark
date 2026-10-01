package com.walkmark.app.domain.cloud

/**
 * Opaque, provider-supplied identity of the account that owns cloud data.
 *
 * WalkMark never mints owner identifiers. The value originates from the identity
 * provider and is mapped in at the composition root, so no domain contract in
 * this package depends on an authentication type.
 */
value class CloudOwnerId(val value: String)