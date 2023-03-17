package com.ecarx.ambridge.utils

import android.content.Context
import android.util.Log
import com.ecarx.ambridge.control.PlaybackControl
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

@Suppress("UNCHECKED_CAST")
class MediaCenterHelper(private val context: Context, private val sdk: DexLib) {
    companion object {
        private val TAG = MediaCenterHelper::class.java.simpleName
        private val PACKAGE_NAME = "com.apple.android.music"
    }
    private val CMediaCenterAPI = sdk.getClass("com.ecarx.sdk.mediacenter.MediaCenterAPIImpl")
    private val CMusicClient = sdk.getClass("com.ecarx.sdk.mediacenter.MusicClient")
    private val CMediaControlClient = sdk.getClass("com.ecarx.sdk.mediacenter.control.MediaControlClient")
    private val CMusicPlaybackInfo = sdk.getClass("com.ecarx.sdk.mediacenter.MusicPlaybackInfo")
    private val IMediaControlClientAPI = sdk.getClass("com.ecarx.sdk.mediacenter.control.IMediaControlClientAPI")
    private val musicClient = CMusicClient.newInstance()

    private var mediaCenterAPI: Any? = null
    private var token: Any? = null
    private var controlToken: Any? = null
    private var mediaControlClientAPI: Any? = null

    private var nopMediaControlClient: Any? = null
    private var nopMusicPlaybackInfo: Any? = null

    fun initMediaCenterApi() {
        val methodGet = CMediaCenterAPI.getMethod("get", Context::class.java as Class<Any>)
        mediaCenterAPI = methodGet.invoke(null, context)
        if (mediaCenterAPI == null) {
            return
        }
        val clzECarXApiClient = sdk.getClass("com.ecarx.sdk.openapi.ECarXApiClient")
        val clzCallback = clzECarXApiClient.getInnerClass("Callback")
        val methodInit = CMediaCenterAPI.getMethod("init", Context::class.java as Class<Any>, clzCallback!!.clazz)

        val derivedCallback = clzCallback.implementThis(context) { builder ->
            builder.method(ElementMatchers.named("onAPIReady"))
                .intercept(MethodDelegation.to(this))
        }

        methodInit.invoke(mediaCenterAPI, context, derivedCallback)
        onAPIReady(true)
    }

    fun onAPIReady(state: Boolean) {
        Log.i(TAG, "MediaCenterAPI onAPIReady called with $state")
        val methodRegisterMusic = CMediaCenterAPI.getMethod(
            "registerMusic",
            String::class.java as Class<Any>,
            CMusicClient.clazz
        )
        token = methodRegisterMusic.invoke(mediaCenterAPI, PACKAGE_NAME, musicClient)
        Log.i(TAG, "Token retrieved ${token as String?}")
        val sourceTypeList = intArrayOf(6)
        val methodUpdateMediaSourceTypeList = CMediaCenterAPI.getMethod(
            "updateMediaSourceTypeList",
            Any::class.java,
            sourceTypeList::class.java as Class<Any>
        )
        methodUpdateMediaSourceTypeList.invoke(mediaCenterAPI, token, sourceTypeList)
        registerMediaControlClient()
        registerMusicPlaybackInfo()
        return
    }

    fun onDestroy() {
        if (token != null) {
            Log.i(TAG, "Unregistering music client")
            val methodUnregister = CMediaCenterAPI.getMethod(
                "unregister",
                Any::class.java
            )
            methodUnregister.invoke(mediaCenterAPI, token)
            token = null
        }
    }

    private fun registerMusicPlaybackInfo() {
        val myMusicPlaybackInfo = PlaybackControl.getInstance(context).musicPlaybackInfo
        nopMusicPlaybackInfo = CMusicPlaybackInfo.extend(context) {
            it.method(ElementMatchers.named("getTitle"))
                .intercept(MethodDelegation.to(myMusicPlaybackInfo))
        }
    }

    private fun registerMediaControlClient() {
        val methodGetMediaControlClientApi = CMediaCenterAPI.getMethod("getMediaControlClientApi")
        mediaControlClientAPI = methodGetMediaControlClientApi.invoke(mediaCenterAPI)
        val methodMCCRegister = IMediaControlClientAPI.getMethod(
            "register",
            String::class.java as Class<Any>,
            CMediaControlClient.clazz,
        )
        val myMediaControlClient = PlaybackControl.getInstance(context).mediaControlClient
        nopMediaControlClient = CMediaControlClient.extend(context) {
            it.method(ElementMatchers.named("onPauseNow"))
                .intercept(MethodDelegation.to(myMediaControlClient))
        }
        controlToken = methodMCCRegister.invoke(mediaControlClientAPI, PACKAGE_NAME, nopMediaControlClient)
        requestControl()
    }

    private fun requestControl() {
        val methodRequestControlled = IMediaControlClientAPI.getMethod(
            "requestControlled",
            Any::class.java
        )
        if (mediaControlClientAPI != null && controlToken != null) {
            methodRequestControlled.invoke(mediaControlClientAPI, controlToken)
            Log.i(TAG, "request apple music media playback controlled by car")
        }
    }
}