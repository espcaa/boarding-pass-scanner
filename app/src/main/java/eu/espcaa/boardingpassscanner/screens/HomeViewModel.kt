package eu.espcaa.boardingpassscanner.screens

import androidx.compose.material3.ColorScheme
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    private val _airlineColors = MutableStateFlow<Map<String, ColorScheme>>(emptyMap())
    val airlineColors: StateFlow<Map<String, ColorScheme>> = _airlineColors.asStateFlow()

    fun cacheColorScheme(carrier: String, scheme: ColorScheme) {
        if (_airlineColors.value.containsKey(carrier)) return
        _airlineColors.update { it + (carrier to scheme) }
    }
}