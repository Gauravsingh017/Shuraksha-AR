package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ENGLISH("en", "English", "🇬🇧"),
    HINDI("hi", "हिन्दी", "🇮🇳")
}

class SurakshaVoiceManager(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "SurakshaVoiceManager"
    private var tts: TextToSpeech? = null

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _activeTag = MutableStateFlow<String?>(null)
    val activeTag: StateFlow<String?> = _activeTag.asStateFlow()

    private val localeHindi = Locale("hi", "IN")
    private val localeEnglish = Locale.US

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                engine.setSpeechRate(0.92f)
                engine.setPitch(1.0f)

                // Configure Utterance Progress Listener
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _activeTag.value = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _activeTag.value = null
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        _activeTag.value = null
                        Log.w(TAG, "TTS Error code: $errorCode for utterance: $utteranceId")
                    }
                })

                _isTtsReady.value = true
                Log.i(TAG, "Suraksha Voice TTS Engine Initialized successfully")
            }
        } else {
            Log.e(TAG, "TTS Initialization failed with status code $status")
            _isTtsReady.value = false
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun toggleLanguage(): AppLanguage {
        val nextLang = if (_currentLanguage.value == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
        _currentLanguage.value = nextLang
        return nextLang
    }

    fun speak(text: String, language: AppLanguage = _currentLanguage.value, tag: String? = null) {
        if (text.isBlank()) return

        val engine = tts
        if (engine == null || !_isTtsReady.value) {
            Log.w(TAG, "TTS Engine not ready yet, skipping speak request")
            return
        }

        try {
            // Apply language locale
            val targetLocale = if (language == AppLanguage.HINDI) localeHindi else localeEnglish
            val result = engine.setLanguage(targetLocale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Target locale $targetLocale not fully supported or missing data, falling back to default")
                if (language == AppLanguage.HINDI) {
                    // Try generic Hindi locale
                    engine.setLanguage(Locale("hi"))
                }
            }

            _activeTag.value = tag
            val utteranceId = tag ?: "suraksha_${System.currentTimeMillis()}"

            // Stop current speech and queue new speech
            engine.stop()
            _isSpeaking.value = true
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing speak command", e)
            _isSpeaking.value = false
            _activeTag.value = null
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
            _activeTag.value = null
        }
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
        _isTtsReady.value = false
    }
}
