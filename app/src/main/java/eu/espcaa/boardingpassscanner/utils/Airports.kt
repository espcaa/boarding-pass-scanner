package eu.espcaa.boardingpassscanner.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AirportInfo(
    val city: String,
    val name: String,
    val lat: Double,
    val lon: Double
)

class AirportManager(private val context: Context) {
    private var airportMap: Map<String, AirportInfo> = emptyMap()
    private var isLoaded = false

    suspend fun loadAirports() = withContext(Dispatchers.IO) {
        if (isLoaded) return@withContext
        try {
            val jsonString =
                context.assets.open("airports.json").bufferedReader().use { it.readText() }
            airportMap = Json.decodeFromString<Map<String, AirportInfo>>(jsonString)
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getInfo(iataCode: String): AirportInfo? {
        return airportMap[iataCode.uppercase()]
    }

    fun getCity(iataCode: String): String = getInfo(iataCode)?.city ?: iataCode
    fun getName(iataCode: String): String = getInfo(iataCode)?.name ?: "Unknown Airport"
}