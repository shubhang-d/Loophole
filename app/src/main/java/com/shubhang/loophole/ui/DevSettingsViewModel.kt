package com.shubhang.loophole.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shubhang.loophole.settings.DevSettingsRepository
import com.shubhang.loophole.settings.SettingsWriteResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DevSettingsViewModel(
    private val repository: DevSettingsRepository,
) : ViewModel() {

    private val permissionDenied = MutableStateFlow(false)

    /**
     * The repository flow is backed by a ContentObserver, so toggles made from
     * the tile, the widget, or the system Settings app land here without the
     * screen having to re-read anything on resume.
     */
    val uiState: StateFlow<DevSettingsUiState> =
        combine(repository.isEnabled, permissionDenied) { isEnabled, denied ->
            DevSettingsUiState(isEnabled = isEnabled, isPermissionDenied = denied)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DevSettingsUiState(),
        )

    fun onToggle() {
        viewModelScope.launch {
            // Toggling in the repository rather than off uiState keeps the write
            // based on the freshly read system value.
            permissionDenied.value = repository.toggle() is SettingsWriteResult.PermissionDenied
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(repository: DevSettingsRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { DevSettingsViewModel(repository) }
            }
    }
}
