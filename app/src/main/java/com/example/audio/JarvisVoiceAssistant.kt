package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class JarvisVoiceCommand {
    object ScanDevices : JarvisVoiceCommand()
    object ShowConnected : JarvisVoiceCommand()
    object OpenMessages : JarvisVoiceCommand()
    data class PrepareMessage(val targetContact: String, val textContent: String) : JarvisVoiceCommand()
    object StartVoiceRecording : JarvisVoiceCommand()
    object DisconnectCurrent : JarvisVoiceCommand()
    object EnableDarkMode : JarvisVoiceCommand()
    object EnableLightMode : JarvisVoiceCommand()
    data class Unknown(val rawText: String) : JarvisVoiceCommand()
}

class JarvisVoiceAssistant(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _readAloudEnabled = MutableStateFlow(false)
    val readAloudEnabled: StateFlow<Boolean> = _readAloudEnabled.asStateFlow()

    private val _voiceEnabled = MutableStateFlow(true)
    val voiceEnabled: StateFlow<Boolean> = _voiceEnabled.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.w("JarvisVoiceAssistant", "TTS initialization failed: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
                // Technological, crisp JARVIS cadence
                engine.setPitch(1.05f)
                engine.setSpeechRate(1.02f)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }
                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
                isTtsReady = true
            }
        }
    }

    fun speakGreeting() {
        speak("As salaam alaikum. JARVIS CONNECT is online. Ready for secure communication.")
    }

    fun speak(text: String, utteranceId: String = "JARVIS_SPEECH") {
        if (!_voiceEnabled.value) return
        if (isTtsReady && tts != null) {
            try {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } catch (e: Exception) {
                Log.w("JarvisVoiceAssistant", "Speech error: ${e.message}")
            }
        }
    }

    fun setReadAloud(enabled: Boolean) {
        _readAloudEnabled.value = enabled
    }

    fun setVoiceEnabled(enabled: Boolean) {
        _voiceEnabled.value = enabled
        if (!enabled) {
            stop()
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun parseVoiceCommand(spokenText: String): JarvisVoiceCommand {
        val normalized = spokenText.trim().lowercase()

        return when {
            normalized.contains("scan") || normalized.contains("search nearby") || normalized.contains("scanner") ->
                JarvisVoiceCommand.ScanDevices

            normalized.contains("show connected") || normalized.contains("connected devices") ->
                JarvisVoiceCommand.ShowConnected

            normalized.contains("open my messages") || normalized.contains("open messages") || normalized.contains("inbox") ->
                JarvisVoiceCommand.OpenMessages

            normalized.contains("start a voice recording") || normalized.contains("record voice") || normalized.contains("record audio") ->
                JarvisVoiceCommand.StartVoiceRecording

            normalized.contains("disconnect") || normalized.contains("drop connection") ->
                JarvisVoiceCommand.DisconnectCurrent

            normalized.contains("dark mode") ->
                JarvisVoiceCommand.EnableDarkMode

            normalized.contains("light mode") ->
                JarvisVoiceCommand.EnableLightMode

            normalized.startsWith("send a message to") || normalized.startsWith("send message to") -> {
                val remainder = normalized.replace("send a message to", "").replace("send message to", "").trim()
                val parts = remainder.split(" saying ", limit = 2)
                val contact = parts.getOrNull(0)?.trim() ?: "Contact"
                val message = parts.getOrNull(1)?.trim() ?: "Tactical check-in"
                JarvisVoiceCommand.PrepareMessage(contact, message)
            }

            else -> JarvisVoiceCommand.Unknown(spokenText)
        }
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
