package eu.espcaa.boardingpassscanner.utils

import android.content.Context
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class AirlineManager(androidContext: Context) {
    private var airlineMap: Map<String, String> = emptyMap()

    fun init(context: Context) {
        try {
            val jsonString =
                context.assets.open("airline_map.json").bufferedReader().use { it.readText() }
            airlineMap = Json.decodeFromString(
                MapSerializer(String.serializer(), String.serializer()),
                jsonString
            )
        } catch (e: Exception) {
            e.printStackTrace()
            airlineMap = emptyMap()
        }
    }

    fun getIcao(iataCode: String): String? {
        return airlineMap[iataCode.uppercase()]
    }
}