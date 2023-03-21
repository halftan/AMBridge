package com.ecarxfan.ambridge.control

import android.content.Context
import android.util.Log
import com.ecarxfan.ambridge.factory.SingletonHolder
import com.ecarxfan.ambridge.utils.MediaControlClient
import com.ecarxfan.ambridge.utils.MusicPlaybackInfo
import java.util.*

class PlaybackControl private constructor(context: Context) {
    companion object : SingletonHolder<PlaybackControl, Context>(::PlaybackControl)

    private var listeners: MutableList<PlaybackEventListener> = mutableListOf()
    val musicPlaybackInfo = MusicPlaybackInfo(context)
    val mediaControlClient = MediaControlClient(context)

    fun register(listener: PlaybackEventListener) {
        listeners.add(listener)
    }

    fun unRegister(listener: PlaybackEventListener) {
        listeners.remove(listener)
    }

    private var lastUpdated: Long = 0L

    fun fire(artist: String, album: String, title: String, artworkUri: String) {
        musicPlaybackInfo.artist = artist
        musicPlaybackInfo.album = album
        musicPlaybackInfo.title = title
        musicPlaybackInfo.artworkUri = artworkUri
        requestUpdate()
    }

    fun requestUpdate() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdated > 500L) {
            lastUpdated = currentTime
            listeners.forEach { it.updateTrackMetadata(
                musicPlaybackInfo.artist,
                musicPlaybackInfo.album,
                musicPlaybackInfo.title,
                musicPlaybackInfo.artworkUri
            ) }
        } else {
            Log.e("PlaybackControl", "Ignoring frequent media update")
        }
    }

    interface PlaybackEventListener: EventListener {
        fun updateTrackMetadata(artist: String, album: String, title: String, artworkUri: String) {

        }
    }
}

