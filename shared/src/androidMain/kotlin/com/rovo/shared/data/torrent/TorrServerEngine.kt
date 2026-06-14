package com.rovo.shared.data.torrent

import android.content.Context
import android.util.Log
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class TorrServerEngine(
    private val context: Context,
    private val client: HttpClient
) {
    companion object {
        private const val TAG = "RovoTorrent"
        private const val PORT = 8090
        private const val BINARY_NAME = "libtorrserver.so"
    }

    private var process: Process? = null

    fun getBaseUrl(): String = "http://127.0.0.1:$PORT"

    suspend fun start() = withContext(Dispatchers.IO) {
        if (isRunning()) {
            Log.d(TAG, "TorrServer already running")
            return@withContext
        }

        val binaryPath = getBinaryPath()
        if (binaryPath == null) {
            throw IllegalStateException("TorrServer binary not found in native library directory")
        }

        val binaryFile = File(binaryPath)
        val abis = android.os.Build.SUPPORTED_ABIS.joinToString()
        Log.i(TAG, "Starting TorrServer: $binaryPath (Size: ${binaryFile.length()}, CanExec: ${binaryFile.canExecute()})")

        val configDir = File(context.filesDir, "torrserver")
        configDir.mkdirs()

        val pb = ProcessBuilder(binaryPath, "-p", PORT.toString(), "-d", configDir.absolutePath)
            .redirectErrorStream(true)
        
        pb.environment()["GODEBUG"] = "netdns=go"
        pb.environment()["HOME"] = configDir.absolutePath
        pb.environment()["TMPDIR"] = configDir.absolutePath
        
        try {
            process = pb.start()
        } catch (e: Exception) {
            Log.e(TAG, "Direct start failed, trying shell wrapper...", e)
            process = ProcessBuilder("sh", "-c", "\"$binaryPath\" -p $PORT -d \"${configDir.absolutePath}\"")
                .redirectErrorStream(true)
                .apply {
                    environment()["GODEBUG"] = "netdns=go"
                    environment()["HOME"] = configDir.absolutePath
                }
                .start()
        }

        // Log output in background
        val proc = process
        Thread({
            try {
                proc?.inputStream?.bufferedReader()?.forEachLine { line ->
                    Log.v(TAG, "TorrServer: $line")
                }
            } catch (_: Exception) {}
        }, "torrserver-log").apply { isDaemon = true }.start()

        // Wait for server to be ready
        val deadline = System.currentTimeMillis() + 15_000L
        while (System.currentTimeMillis() < deadline) {
            if (echo()) {
                Log.d(TAG, "TorrServer started successfully on port $PORT")
                return@withContext
            }
            
            if (process?.isAlive == false) {
                val exitCode = process?.exitValue()
                throw IllegalStateException("TorrServer failed with exit code $exitCode")
            }
            Thread.sleep(500)
        }

        throw IllegalStateException("TorrServer failed to start within 15 seconds")
    }

    fun stop() {
        try {
            process?.let { proc ->
                val exited = proc.waitFor(3, TimeUnit.SECONDS)
                if (!exited) {
                    proc.destroyForcibly()
                    Log.w(TAG, "TorrServer force-killed")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TorrServer", e)
            process?.destroyForcibly()
        } finally {
            process = null
            Log.d(TAG, "TorrServer stopped")
        }
    }

    suspend fun isRunning(): Boolean {
        val proc = process ?: return false
        return proc.isAlive && echo()
    }

    suspend fun echo(): Boolean {
        return try {
            val response = client.get("${getBaseUrl()}/echo")
            response.status.value in 200..299
        } catch (_: Exception) {
            false
        }
    }

    private fun getBinaryPath(): String? {
        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        val binary = File(nativeLibDir, BINARY_NAME)
        return if (binary.exists()) binary.absolutePath else null
    }
}
