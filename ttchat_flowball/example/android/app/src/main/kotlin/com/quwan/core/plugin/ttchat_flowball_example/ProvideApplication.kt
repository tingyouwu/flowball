package com.quwan.core.plugin.ttchat_flowball_example

import io.flutter.app.FlutterApplication
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

open class ProvideApplication : FlutterApplication() {

    private var myApplication: ProvideApplication? = null

    open fun getApplication(): ProvideApplication? {
        return myApplication
    }

    lateinit var flutterEngine : FlutterEngine

    companion object {
        const val EngineId = "tt_engine_id"
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this
        // 提供共享引擎
        proviceShareFlutterEngine();
    }

    private fun proviceShareFlutterEngine() {
        // 提前创建共享引擎
        // Instantiate a FlutterEngine.
        flutterEngine = FlutterEngine(this, arrayOf("--verbose-logging"), true)

        flutterEngine.navigationChannel.setInitialRoute("/")
        // Start executing Dart code to pre-warm the FlutterEngine.
        flutterEngine.dartExecutor.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        )

        // Cache the FlutterEngine to be used by FlutterActivity.
        FlutterEngineCache
                .getInstance()
                .put(EngineId, flutterEngine)
    }
}
