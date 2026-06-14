package com.rovo.app.data.torrent

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rovo.shared.api.TorrServerApi
import com.rovo.shared.data.torrent.TorrServerEngine
import com.rovo.shared.model.torrent.TorrentProgress
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class TorrentService : Service() {

    private val engine: TorrServerEngine by inject()
    private val api: TorrServerApi by inject()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var downloadJob: Job? = null
    private var currentMagnet: String? = null

    companion object {
        private const val TAG = "RovoTorrent"
        private const val PRELOAD_TARGET_BYTES = 5_242_880f
        var onStreamReady: ((String) -> Unit)? = null
        var onStreamError: ((String) -> Unit)? = null
        var onStreamProgress: ((TorrentProgress) -> Unit)? = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val magnetLink = intent?.getStringExtra("MAGNET_LINK") ?: return START_NOT_STICKY
        val fileIdx = intent.getIntExtra("FILE_IDX", -1)
        val fileName = intent.getStringExtra("FILE_NAME") ?: ""

        try {
            startForegroundService()
            startDownload(magnetLink, fileIdx, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Critical error starting service: ${e.message}")
            scope.launch(Dispatchers.Main) {
                onStreamError?.invoke(e.message ?: "Failed to start torrent engine")
            }
            stopSelf()
        }

        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "torrent_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Torrent Download", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rovo Streaming")
            .setContentText("Starting torrent engine...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }

    private fun startDownload(magnet: String, fileIdx: Int, fileName: String = "") {
        downloadJob?.cancel()

        val previousMagnet = currentMagnet
        currentMagnet = magnet

        downloadJob = scope.launch {
            if (previousMagnet != null && previousMagnet != magnet) {
                api.dropTorrent(previousMagnet)
            }
            try {
                withContext(Dispatchers.Main) {
                    onStreamProgress?.invoke(TorrentProgress(status = "Starting engine..."))
                }
                engine.start()

                withContext(Dispatchers.Main) {
                    onStreamProgress?.invoke(TorrentProgress(status = "Fetching metadata..."))
                }
                api.addTorrent(magnet)

                val targetFileIndex = resolveFileIndex(magnet, fileIdx, fileName)
                val streamUrl = api.getStreamUrl(magnet, targetFileIndex)
                
                updateNotification("Streaming...")
                withContext(Dispatchers.Main) {
                    onStreamProgress?.invoke(TorrentProgress(status = "Starting playback..."))
                    onStreamReady?.invoke(streamUrl)
                }

                while (isActive) {
                    delay(1000)
                    try {
                        val stats = api.getTorrentStats(magnet)
                        val progress = if (stats.preloaded_bytes in 1 until PRELOAD_TARGET_BYTES.toLong()) {
                            stats.preloaded_bytes.toFloat() / PRELOAD_TARGET_BYTES
                        } else null
                        
                        withContext(Dispatchers.Main) {
                            onStreamProgress?.invoke(
                                TorrentProgress(
                                    status = stats.statusText(),
                                    downloadSpeed = stats.download_speed,
                                    peers = stats.active_peers,
                                    seeds = stats.connected_seeders,
                                    progress = progress
                                )
                            )
                        }
                    } catch (_: Exception) {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in download: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onStreamError?.invoke("Torrent error: ${e.message}")
                }
                stopSelf()
            }
        }
    }

    private val videoExtensions = setOf("mkv", "mp4", "avi", "webm", "ts", "m4v", "mov", "wmv", "flv")

    private suspend fun resolveFileIndex(magnet: String, hintIdx: Int, hintName: String = ""): Int {
        val deadline = System.currentTimeMillis() + 15_000L
        while (System.currentTimeMillis() < deadline) {
            val files = api.getFileList(magnet)
            if (files.isNotEmpty()) {
                val videoFiles = files.filter { f ->
                    val ext = f.path.substringAfterLast('.', "").lowercase()
                    ext in videoExtensions
                }
                
                if (hintName.isNotEmpty()) {
                    val byName = videoFiles.firstOrNull {
                        it.path.endsWith(hintName, ignoreCase = true) ||
                        it.path.substringAfterLast('/').equals(hintName, ignoreCase = true)
                    }
                    if (byName != null) return byName.id
                }
                
                if (hintIdx >= 0) {
                    val byId = videoFiles.firstOrNull { it.id == hintIdx + 1 }
                    if (byId != null) return byId.id
                    
                    if (hintIdx < files.size) {
                        val byPos = files[hintIdx]
                        val ext = byPos.path.substringAfterLast('.', "").lowercase()
                        if (ext in videoExtensions) return byPos.id
                    }
                }
                
                val target = videoFiles.maxByOrNull { it.length }
                    ?: files.maxByOrNull { it.length }!!
                return target.id
            }
            delay(500)
        }
        return hintIdx.coerceAtLeast(0)
    }

    private fun updateNotification(text: String) {
        val channelId = "torrent_channel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rovo Streaming")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
        getSystemService(NotificationManager::class.java)?.notify(1, notification)
    }

    override fun onDestroy() {
        downloadJob?.cancel()
        runBlocking(Dispatchers.IO) {
            currentMagnet?.let { api.dropTorrent(it) }
            engine.stop()
        }
        currentMagnet = null
        job.cancel()
        onStreamReady = null
        onStreamError = null
        onStreamProgress = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
