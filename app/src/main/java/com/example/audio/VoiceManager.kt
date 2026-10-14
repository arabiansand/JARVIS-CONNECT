package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class VoiceRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    fun startRecording(fileNamePrefix: String = "jarvis_voice"): File? {
        if (_isRecording.value) return currentOutputFile

        val outputDir = File(context.cacheDir, "voice_memos").apply { mkdirs() }
        val file = File(outputDir, "${fileNamePrefix}_${System.currentTimeMillis()}.m4a")
        currentOutputFile = file

        return try {
            val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            rec.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = rec
            _isRecording.value = true
            _recordingDurationSeconds.value = 0

            recordingJob = scope.launch {
                var seconds = 0
                while (isActive && _isRecording.value) {
                    delay(100)
                    try {
                        _amplitude.value = recorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        _amplitude.value = 0
                    }
                    if (seconds % 10 == 0) {
                        _recordingDurationSeconds.value = seconds / 10
                    }
                    seconds++
                }
            }
            file
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Failed to start recording: ${e.message}")
            stopRecording(discard = true)
            null
        }
    }

    fun stopRecording(discard: Boolean = false): File? {
        recordingJob?.cancel()
        recordingJob = null
        val file = currentOutputFile
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w("VoiceRecorderManager", "Error stopping recorder: ${e.message}")
        } finally {
            recorder = null
            _isRecording.value = false
            _amplitude.value = 0
            if (discard && file?.exists() == true) {
                file.delete()
                currentOutputFile = null
                return null
            }
        }
        return file
    }
}

class VoicePlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingPath = MutableStateFlow<String?>(null)
    val currentPlayingPath: StateFlow<String?> = _currentPlayingPath.asStateFlow()

    private val _progressFraction = MutableStateFlow(0f)
    val progressFraction: StateFlow<Float> = _progressFraction.asStateFlow()

    fun playAudio(path: String, onFinished: (() -> Unit)? = null) {
        val file = File(path)
        if (!file.exists()) return

        stopAudio()

        try {
            val player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
            }
            mediaPlayer = player
            _isPlaying.value = true
            _currentPlayingPath.value = path

            player.setOnCompletionListener {
                stopAudio()
                onFinished?.invoke()
            }

            player.setOnErrorListener { _, _, _ ->
                stopAudio()
                true
            }

            progressJob = scope.launch {
                while (isActive && _isPlaying.value && player.isPlaying) {
                    val duration = player.duration.coerceAtLeast(1)
                    val pos = player.currentPosition
                    _progressFraction.value = (pos.toFloat() / duration).coerceIn(0f, 1f)
                    delay(100)
                }
            }
        } catch (e: Exception) {
            Log.e("VoicePlayerManager", "Failed to play audio: ${e.message}")
            stopAudio()
        }
    }

    fun stopAudio() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w("VoicePlayerManager", "Error stopping player: ${e.message}")
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
            _currentPlayingPath.value = null
            _progressFraction.value = 0f
        }
    }
}
