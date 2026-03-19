package eu.espcaa.boardingpassscanner.utils

import androidx.compose.material3.ColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AirlineColorCache {

    private val _colors = MutableStateFlow<Map<String, ColorScheme>>(emptyMap())
    val colors: StateFlow<Map<String, ColorScheme>> = _colors.asStateFlow()

    fun cacheColorScheme(carrier: String, scheme: ColorScheme) {
        if (_colors.value.containsKey(carrier)) return
        _colors.update { it + (carrier to scheme) }
    }

    fun get(carrier: String): ColorScheme? = _colors.value[carrier]
}
