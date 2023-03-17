package com.ecarx.ambridge.control

import android.content.Context
import com.ecarx.ambridge.factory.SingletonHolder
import com.ecarx.ambridge.utils.MediaControlClient
import com.ecarx.ambridge.utils.MusicPlaybackInfo
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

    private var lastTitle: String = ""
    private var lastAlbum: String = ""
    private var lastArtist: String = ""
    private var lastArtworkUri: String = ""

    fun fire(artist: String, album: String, title: String, artworkUri: String) {
        if (musicPlaybackInfo.title == title
            && musicPlaybackInfo.album == album
            && musicPlaybackInfo.artist == artist
            && musicPlaybackInfo.artworkUri == artworkUri) {
            return
        }
        musicPlaybackInfo.artist = artist
        musicPlaybackInfo.album = album
        musicPlaybackInfo.title = title
        musicPlaybackInfo.artworkUri = artworkUri
        requestUpdate()
    }

    fun requestUpdate() {
        listeners.forEach { it.updateTrackMetadata(
            musicPlaybackInfo.artist,
            musicPlaybackInfo.album,
            musicPlaybackInfo.title,
            musicPlaybackInfo.artworkUri
        ) }
    }

    interface PlaybackEventListener: EventListener {
        fun updateTrackMetadata(artist: String, album: String, title: String, artworkUri: String) {

        }
    }
}

