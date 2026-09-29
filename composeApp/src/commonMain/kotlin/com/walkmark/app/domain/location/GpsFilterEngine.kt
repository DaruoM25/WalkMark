package com.walkmark.app.domain.location

class GpsFilterEngine(
    val maxAcceptedAccuracyMeters: Float = 20.0f,
    val maxReasonableSpeedMps: Double = 8.33 // 30 km/h
) {
    /**
     * Evaluates whether a new candidate point should be accepted into the recorded track.
     */
    fun shouldAccept(candidate: LocationPoint, lastAccepted: LocationPoint?): Boolean {
        // 1. Basic coordinate sanity
        if (candidate.latitude < -90.0 || candidate.latitude > 90.0) return false
        if (candidate.longitude < -180.0 || candidate.longitude > 180.0) return false

        // 2. Accuracy threshold gate
        if (candidate.accuracy <= 0.0f || candidate.accuracy > maxAcceptedAccuracyMeters) {
            return false
        }

        // If this is the initial point of the track, accept it after coordinate & accuracy check
        if (lastAccepted == null) {
            return true
        }

        // 3. Temporal monotonicity: candidate must have strictly later timestamp
        val timeDeltaMs = candidate.timestamp - lastAccepted.timestamp
        if (timeDeltaMs <= 0L) {
            return false
        }

        val timeDeltaSec = timeDeltaMs / 1000.0

        // 4. Distance and physical speed jump calculation
        val distanceMeters = DistanceEngine.calculateDistance(lastAccepted, candidate)

        // Stationary jitter threshold: small jitter with tiny displacement
        if (distanceMeters < 0.5) {
            return false
        }

        val derivedSpeedMps = distanceMeters / timeDeltaSec
        if (derivedSpeedMps > maxReasonableSpeedMps) {
            return false
        }

        return true
    }
}
