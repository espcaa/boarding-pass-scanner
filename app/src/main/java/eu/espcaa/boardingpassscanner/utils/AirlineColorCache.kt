package eu.espcaa.boardingpassscanner.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import eu.espcaa.boardingpassscanner.data.AirlineColorDao
import eu.espcaa.boardingpassscanner.data.AirlineColorEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AirlineColorCache(private val dao: AirlineColorDao) {

    private val _colors = MutableStateFlow<Map<String, ColorScheme>>(emptyMap())
    val colors: StateFlow<Map<String, ColorScheme>> = _colors.asStateFlow()

    private val seedColors = mutableMapOf<String, Int>()

    suspend fun loadFromDb(isDark: Boolean) {
        val entities = dao.getAll()
        val loaded = mutableMapOf<String, ColorScheme>()
        for (entity in entities) {
            seedColors[entity.carrier] = entity.seedColor
            loaded[entity.carrier] = dynamicColorScheme(
                seedColor = Color(entity.seedColor),
                isDark = isDark,
                style = PaletteStyle.Vibrant
            )
        }
        _colors.update { it + loaded }
    }

    suspend fun cacheColorScheme(carrier: String, seedColor: Color, scheme: ColorScheme) {
        if (_colors.value.containsKey(carrier)) return
        _colors.update { it + (carrier to scheme) }
        val argb = seedColor.toArgb()
        seedColors[carrier] = argb
        dao.insert(AirlineColorEntity(carrier = carrier, seedColor = argb))
    }

    fun get(carrier: String): ColorScheme? = _colors.value[carrier]
}
