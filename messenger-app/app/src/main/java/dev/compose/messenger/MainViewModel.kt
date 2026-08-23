package dev.compose.messenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.datastore.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _season = MutableStateFlow(Season.SPRING)
    val season: StateFlow<Season> = _season.asStateFlow()

    init {
        viewModelScope.launch {
            val savedSeason = preferencesManager.currentSeason.first()
            if (savedSeason != null) {
                _season.value = Season.valueOf(savedSeason)
            }
        }
    }

    fun changeSeason(season: Season) {
        _season.value = season
        viewModelScope.launch {
            preferencesManager.saveSeason(season.name)
        }
    }
}
