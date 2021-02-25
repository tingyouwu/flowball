package com.quwan.core.plugin.ttchat_flowball

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.quwan.core.plugin.ttchat_flowball.activity.MainActivity
import com.quwan.core.plugin.ttchat_flowball.service.FlowBallService
import com.quwan.core.plugin.ttchat_flowball.service.FlowBallService.INTENT_EXTRA_IS_CLOSE_WINDOW
import com.quwan.core.plugin.ttchat_flowball.service.FlowBallService.INTENT_EXTRA_IS_UPDATE_WINDOW
import com.quwan.core.plugin.ttchat_flowball.utils.Commons
import com.quwan.core.plugin.ttchat_flowball.utils.Constants.CHANNEL
import com.quwan.core.plugin.ttchat_flowball.utils.Constants.INTENT_EXTRA_PARAMS_MAP
import com.quwan.core.plugin.ttchat_flowball.utils.NotificationHelper
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback
import java.util.*

/** TtchatFlowballPlugin */
class TtchatFlowballPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {

  companion object {
    const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1237
    const val TAG = "TtchatFlowballPlugin"
  }

  private var mContext: Context? = null

  @SuppressLint("StaticFieldLeak")
  private var mActivity: Activity? = null

  private var sPluginRegistrantCallback: PluginRegistrantCallback? = null

  var methodChannel: MethodChannel? = null

  private val notificationManager: NotificationManager? = null

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getPlatformVersion" -> result.success("Android " + Build.VERSION.RELEASE)
      "requestPermissions" -> if (askPermission()) {
        result.success(true)
      } else {
        result.success(false)
      }
      "checkPermissions" -> if (checkPermission()) {
        result.success(true)
      } else {
        result.success(false)
      }
      "showSystemWindow" -> if (checkPermission()) {
        val arguments = call.arguments as List<*>
        val title = arguments[0] as String
        val body = arguments[1] as String
        val params = arguments[2] as HashMap<String, Any>
        if (Commons.isForceAndroidBubble(mContext)) {
          Log.d(TAG, "Going to show Bubble")
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showBubble(title, body, params)
          }
        } else {
          Log.d(TAG, "Going to show System Alert Window")
          val i = Intent(mContext, FlowBallService::class.java)
          i.putExtra(INTENT_EXTRA_PARAMS_MAP, params)
          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
          i.putExtra(INTENT_EXTRA_IS_UPDATE_WINDOW, false)
          mContext!!.startService(i)

//          Log.d(TAG, "Going to show System Alert Window")
//          val i = Intent(mContext, MainActivity::class.java)
//          i.putExtra(INTENT_EXTRA_PARAMS_MAP, params)
////          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////          i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//          i.putExtra(INTENT_EXTRA_IS_UPDATE_WINDOW, false)
//          mContext!!.startActivity(i)
        }
        result.success(true)
      } else {
        Toast.makeText(mContext, "Please give draw over other apps permission", Toast.LENGTH_LONG).show()
        result.success(false)
      }
      "updateSystemWindow" -> if (checkPermission()) {
        val updateArguments = call.arguments as List<*>
        val updateTitle = updateArguments[0] as String
        val updateBody = updateArguments[1] as String
        val updateParams = updateArguments[2] as HashMap<String, Any>
        if (Commons.isForceAndroidBubble(mContext) || Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
          Log.d(TAG, "Going to update Bubble")
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showBubble(updateTitle, updateBody, updateParams)
          }
        } else {
          Log.d(TAG, "Going to update System Alert Window")
          val i = Intent(mContext, FlowBallService::class.java)
          i.putExtra(INTENT_EXTRA_PARAMS_MAP, updateParams)
          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
          i.putExtra(INTENT_EXTRA_IS_UPDATE_WINDOW, true)
          mContext!!.startService(i)
        }
        result.success(true)
      } else {
        Toast.makeText(mContext, "Please give draw over other apps permission", Toast.LENGTH_LONG).show()
        result.success(false)
      }
      "closeSystemWindow" -> if (checkPermission()) {
        if (Commons.isForceAndroidBubble(mContext) || Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
          NotificationHelper.getInstance(mContext).dismissNotification()
        } else {
          val i = Intent(mContext, FlowBallService::class.java)
          i.putExtra(INTENT_EXTRA_IS_CLOSE_WINDOW, true)
          mContext!!.startService(i)
        }
        result.success(true)
      } else {
        Toast.makeText(mContext, "Please give draw over other apps permission", Toast.LENGTH_LONG).show()
        result.success(false)
      }
      else -> result.notImplemented()
    }
  }

  fun setPluginRegistrant(callback: PluginRegistrantCallback?) {
    sPluginRegistrantCallback = callback
  }

  private fun invokeCallBackToFlutter(channel: MethodChannel, method: String, arguments: List<Any>, retries: IntArray) {
    channel.invokeMethod(method, arguments, object : Result {
      override fun success(o: Any?) {
        Log.i(TAG, "Invoke call back success")
      }

      override fun error(s: String, s1: String?, o: Any?) {
        Log.e(TAG, "Error $s$s1")
      }

      override fun notImplemented() {
        //To fix the dart initialization delay.
        if (retries[0] > 0) {
          Log.d(TAG, "Not Implemented method $method. Trying again to check if it works")
          invokeCallBackToFlutter(channel, method, arguments, retries)
        } else {
          Log.e(TAG, "Not Implemented method $method")
        }
        retries[0]--
      }
    })
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  fun askPermission(): Boolean {
    if (Commons.isForceAndroidBubble(mContext) || Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
      return NotificationHelper.getInstance(mContext).areBubblesAllowed()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!Settings.canDrawOverlays(mContext)) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + mContext!!.packageName))
        if (mActivity == null) {
          if (mContext != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mContext!!.startActivity(intent)
            Toast.makeText(mContext, "Please grant, Can Draw Over Other Apps permission.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Can't detect the permission change, as the mActivity is null")
          } else {
            Log.e(TAG, "'Can Draw Over Other Apps' permission is not granted")
            Toast.makeText(mContext, "Can Draw Over Other Apps permission is required. Please grant it from the app settings", Toast.LENGTH_LONG).show()
          }
        } else {
          mActivity!!.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
        }
      } else {
        return true
      }
    }
    return false
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  fun checkPermission(): Boolean {
    if (Commons.isForceAndroidBubble(mContext) || Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
      //return NotificationHelper.getInstance(mContext).areBubblesAllowed();
      return true
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return Settings.canDrawOverlays(mContext)
    }
    return false
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private fun showBubble(title: String, body: String, params: HashMap<String, Any>) {
//    val icon = Icon.createWithResource(mContext, R.drawable.ic_notification)
//    val notificationHelper: NotificationHelper = NotificationHelper.getInstance(mContext)
//    notificationHelper.showNotification(icon, title, body, params)
  }

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    mContext = binding.applicationContext
    methodChannel = MethodChannel(binding.binaryMessenger, CHANNEL)
    methodChannel?.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel?.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    this.mActivity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    this.mActivity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    this.mActivity = binding.activity
  }

  override fun onDetachedFromActivity() {
    this.mActivity = null
  }
}
