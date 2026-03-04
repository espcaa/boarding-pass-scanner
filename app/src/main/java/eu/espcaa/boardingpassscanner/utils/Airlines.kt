package eu.espcaa.boardingpassscanner.utils

import android.content.Context

class AirlineManager(androidContext: Context) {
    private var airlineMap: Map<String, String> = emptyMap()

    fun init(context: Context) {
        try {
            val jsonString =
                context.assets.open("airline_map.json").bufferedReader().use { it.readText() }
            airlineMap = kotlinx.serialization.json.Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            airlineMap = emptyMap()
        }
    }

    fun getIcao(iataCode: String): String? {
        return airlineMap[iataCode.uppercase()]
    }
}