package com.example

import android.app.Application
import com.example.audio.JarvisVoiceAssistant
import com.example.audio.VoicePlayerManager
import com.example.audio.VoiceRecorderManager
import com.example.data.JarvisDatabase
import com.example.data.JarvisRepository
import com.example.transport.TransportManager

class JarvisApp : Application() {

    lateinit var database: JarvisDatabase
        private set

    lateinit var repository: JarvisRepository
        private set

    lateinit var transportManager: TransportManager
        private set

    lateinit var voiceRecorderManager: VoiceRecorderManager
        private set

    lateinit var voicePlayerManager: VoicePlayerManager
        private set

    lateinit var voiceAssistant: JarvisVoiceAssistant
        private set

    override fun onCreate() {
        super.onCreate()
        database = JarvisDatabase.getInstance(this)
        repository = JarvisRepository(database)
        transportManager = TransportManager(this)
        voiceRecorderManager = VoiceRecorderManager(this)
        voicePlayerManager = VoicePlayerManager()
        voiceAssistant = JarvisVoiceAssistant(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        transportManager.cleanup()
        voiceAssistant.destroy()
        voicePlayerManager.stopAudio()
    }
}
