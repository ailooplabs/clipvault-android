package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClipDatabase
import com.example.data.ClipItem
import com.example.data.ClipRepository
import com.example.data.ClipType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterTab(val label: String) {
    ALL("All"),
    PINNED("★ Pinned"),
    LINKS("Links"),
    CODE("Code & Commands"),
    COLORS("Colors"),
    SECRETS("Secrets")
}

data class ClipVaultUiState(
    val clips: List<ClipItem> = emptyList(),
    val totalCount: Int = 0,
    val pinnedCount: Int = 0,
    val searchQuery: String = "",
    val activeTab: FilterTab = FilterTab.ALL,
    val editingClip: ClipItem? = null,
    val showAddDialog: Boolean = false,
    val showClearConfirmDialog: Boolean = false,
    val showFeatureTour: Boolean = false,
    val showUseCases: Boolean = false,
    val showPrivacyInfo: Boolean = false,
    val showSettings: Boolean = false,
    val snackbarMessage: String? = null,
    val autoTrimWhitespace: Boolean = true,
    val hapticFeedback: Boolean = true,
    val maskSecretsByDefault: Boolean = true
)

class ClipVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClipRepository
    private val clipboardManager: ClipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val prefs = application.getSharedPreferences("clipvault_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeTab = MutableStateFlow(FilterTab.ALL)
    val activeTab: StateFlow<FilterTab> = _activeTab.asStateFlow()

    private val _editingClip = MutableStateFlow<ClipItem?>(null)
    val editingClip: StateFlow<ClipItem?> = _editingClip.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _showClearConfirmDialog = MutableStateFlow(false)
    val showClearConfirmDialog: StateFlow<Boolean> = _showClearConfirmDialog.asStateFlow()

    private val _showFeatureTour = MutableStateFlow(false)
    val showFeatureTour: StateFlow<Boolean> = _showFeatureTour.asStateFlow()

    private val _showUseCases = MutableStateFlow(false)
    val showUseCases: StateFlow<Boolean> = _showUseCases.asStateFlow()

    private val _showPrivacyInfo = MutableStateFlow(false)
    val showPrivacyInfo: StateFlow<Boolean> = _showPrivacyInfo.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _autoTrimWhitespace = MutableStateFlow(prefs.getBoolean("auto_trim", true))
    val autoTrimWhitespace: StateFlow<Boolean> = _autoTrimWhitespace.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean("haptics", true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _maskSecretsByDefault = MutableStateFlow(prefs.getBoolean("mask_secrets", true))
    val maskSecretsByDefault: StateFlow<Boolean> = _maskSecretsByDefault.asStateFlow()

    private var lastObservedClipboardText: String? = null

    init {
        val database = ClipDatabase.getDatabase(application, viewModelScope)
        repository = ClipRepository(database.clipDao())
    }

    val uiState: StateFlow<ClipVaultUiState> = combine(
        repository.allClips,
        repository.totalCount,
        repository.pinnedCount,
        _searchQuery,
        _activeTab,
        _editingClip,
        _showAddDialog,
        _showClearConfirmDialog,
        _showFeatureTour,
        _showUseCases,
        _showPrivacyInfo,
        _showSettings,
        _snackbarMessage,
        _autoTrimWhitespace,
        _hapticFeedback,
        _maskSecretsByDefault
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val allClips = params[0] as List<ClipItem>
        val totalCount = params[1] as Int
        val pinnedCount = params[2] as Int
        val query = params[3] as String
        val tab = params[4] as FilterTab
        val editing = params[5] as ClipItem?
        val showAdd = params[6] as Boolean
        val showClear = params[7] as Boolean
        val showTour = params[8] as Boolean
        val showUseCases = params[9] as Boolean
        val showPrivacy = params[10] as Boolean
        val showSettings = params[11] as Boolean
        val snackbar = params[12] as String?
        val autoTrim = params[13] as Boolean
        val haptics = params[14] as Boolean
        val maskSecrets = params[15] as Boolean

        val filteredClips = allClips.filter { clip ->
            // Filter by search query
            val matchesSearch = if (query.isBlank()) true else {
                clip.content.contains(query, ignoreCase = true)
            }
            // Filter by active tab
            val matchesTab = when (tab) {
                FilterTab.ALL -> true
                FilterTab.PINNED -> clip.isPinned
                FilterTab.LINKS -> clip.type == ClipType.URL
                FilterTab.CODE -> clip.type == ClipType.CODE
                FilterTab.COLORS -> clip.type == ClipType.COLOR_HEX
                FilterTab.SECRETS -> clip.type == ClipType.SECRET
            }
            matchesSearch && matchesTab
        }

        ClipVaultUiState(
            clips = filteredClips,
            totalCount = totalCount,
            pinnedCount = pinnedCount,
            searchQuery = query,
            activeTab = tab,
            editingClip = editing,
            showAddDialog = showAdd,
            showClearConfirmDialog = showClear,
            showFeatureTour = showTour,
            showUseCases = showUseCases,
            showPrivacyInfo = showPrivacy,
            showSettings = showSettings,
            snackbarMessage = snackbar,
            autoTrimWhitespace = autoTrim,
            hapticFeedback = haptics,
            maskSecretsByDefault = maskSecrets
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClipVaultUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(tab: FilterTab) {
        _activeTab.value = tab
    }

    fun openAddDialog() {
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun openEditDialog(clip: ClipItem) {
        _editingClip.value = clip
    }

    fun closeEditDialog() {
        _editingClip.value = null
    }

    fun openClearConfirmDialog() {
        _showClearConfirmDialog.value = true
    }

    fun closeClearConfirmDialog() {
        _showClearConfirmDialog.value = false
    }

    fun openFeatureTour() {
        _showFeatureTour.value = true
    }

    fun closeFeatureTour() {
        _showFeatureTour.value = false
    }

    fun openUseCases() {
        _showUseCases.value = true
    }

    fun closeUseCases() {
        _showUseCases.value = false
    }

    fun openPrivacyInfo() {
        _showPrivacyInfo.value = true
    }

    fun closePrivacyInfo() {
        _showPrivacyInfo.value = false
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun triggerHapticFeedback() {
        if (!_hapticFeedback.value) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    fun copyClipToClipboard(clip: ClipItem) {
        val clipData = ClipData.newPlainText("ClipVault", clip.content)
        clipboardManager.setPrimaryClip(clipData)
        lastObservedClipboardText = clip.content
        triggerHapticFeedback()

        viewModelScope.launch {
            repository.recordCopy(clip.id)
            _snackbarMessage.value = "Copied to clipboard!"
        }
    }

    fun addClip(content: String, isPinned: Boolean = false) {
        val textToSave = if (_autoTrimWhitespace.value) content.trim() else content
        if (textToSave.isBlank()) return

        viewModelScope.launch {
            repository.insert(textToSave, isPinned = isPinned)
            _showAddDialog.value = false
            triggerHapticFeedback()
            _snackbarMessage.value = "Clip saved to ClipVault"
        }
    }

    fun saveEditedClip(clip: ClipItem, newContent: String, copyAfterSave: Boolean = false) {
        val textToSave = if (_autoTrimWhitespace.value) newContent.trim() else newContent
        if (textToSave.isBlank()) return

        val newType = ClipItem.detectType(textToSave)
        val updated = clip.copy(
            content = textToSave,
            type = newType,
            characterCount = textToSave.length
        )

        viewModelScope.launch {
            repository.update(updated)
            _editingClip.value = null
            triggerHapticFeedback()

            if (copyAfterSave) {
                copyClipToClipboard(updated)
                _snackbarMessage.value = "Updated & copied to clipboard"
            } else {
                _snackbarMessage.value = "Clip updated"
            }
        }
    }

    fun togglePin(clip: ClipItem) {
        viewModelScope.launch {
            repository.togglePin(clip.id, clip.isPinned)
            triggerHapticFeedback()
            _snackbarMessage.value = if (!clip.isPinned) "★ Pinned to top" else "Unpinned"
        }
    }

    fun toggleMask(clip: ClipItem) {
        viewModelScope.launch {
            repository.toggleMask(clip.id, clip.isMasked)
            triggerHapticFeedback()
        }
    }

    fun deleteClip(clip: ClipItem) {
        viewModelScope.launch {
            repository.delete(clip)
            triggerHapticFeedback()
            _snackbarMessage.value = "Clip deleted"
        }
    }

    fun clearUnpinnedClips() {
        viewModelScope.launch {
            repository.clearUnpinned()
            _showClearConfirmDialog.value = false
            triggerHapticFeedback()
            _snackbarMessage.value = "Cleared unpinned history. Pinned clips preserved!"
        }
    }

    fun clearAllClips() {
        viewModelScope.launch {
            repository.clearAll()
            _showClearConfirmDialog.value = false
            triggerHapticFeedback()
            _snackbarMessage.value = "All clips cleared"
        }
    }

    fun resetToStarterPack() {
        viewModelScope.launch {
            repository.populateStarterPack()
            triggerHapticFeedback()
            _snackbarMessage.value = "Starter clips restored!"
        }
    }

    fun captureCurrentClipboard(): Boolean {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0)?.text?.toString()
            if (!text.isNullOrBlank() && text != lastObservedClipboardText) {
                lastObservedClipboardText = text
                addClip(text)
                return true
            }
        }
        return false
    }

    fun syncClipboardOnResume() {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0)?.text?.toString()
            if (!text.isNullOrBlank() && text != lastObservedClipboardText) {
                // If this is the first run, record it
                if (lastObservedClipboardText != null) {
                    lastObservedClipboardText = text
                    addClip(text)
                } else {
                    lastObservedClipboardText = text
                }
            }
        }
    }

    fun setAutoTrim(enabled: Boolean) {
        _autoTrimWhitespace.value = enabled
        prefs.edit().putBoolean("auto_trim", enabled).apply()
    }

    fun setHaptics(enabled: Boolean) {
        _hapticFeedback.value = enabled
        prefs.edit().putBoolean("haptics", enabled).apply()
    }

    fun setMaskSecrets(enabled: Boolean) {
        _maskSecretsByDefault.value = enabled
        prefs.edit().putBoolean("mask_secrets", enabled).apply()
    }
}
