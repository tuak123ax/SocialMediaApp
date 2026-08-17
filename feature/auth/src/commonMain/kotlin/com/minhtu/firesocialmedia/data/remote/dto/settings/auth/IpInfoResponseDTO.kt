package com.minhtu.firesocialmedia.data.remote.dto.settings.auth

import kotlinx.serialization.Serializable

@Serializable
data class IpInfoResponseDTO(
    val ip: String? = null,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val loc: String? = null,
    val org: String? = null,
    val timezone: String? = null
) {
    fun locationInfo(): String {
        val cleanCity = city?.takeIf { it.isNotBlank() }

        val cleanRegion = region?.takeIf {
            it.isNotBlank() &&
                    !it.equals(cleanCity, ignoreCase = true) &&
                    !it.contains(cleanCity ?: "", ignoreCase = true)
        }

        val cleanCountry = country?.takeIf { it.isNotBlank() }

        return listOfNotNull(
            cleanCity,
            cleanRegion,
            cleanCountry
        ).joinToString(", ")
            .ifBlank { "Unknown location" }
    }
}
