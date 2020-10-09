package com.example.flutter_play_asset

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull;
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/** FlutterPlayAssetPlugin */
public class FlutterPlayAssetPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var ctx: Context
  val FLUTTER_METHOD_PLAYASSET_DOWNLOAD = "playasset"
  val FLUTTER_METHOD_DOWNLOAD_PROGRESS_UPDATE = "playasset_download_pprogress_update"
  val CHANNEL = "basictomodular/downloadservice"
  lateinit var methodChannel: MethodChannel
  lateinit var assetPackManager: AssetPackManager
  lateinit var package_name : String

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), CHANNEL)
    channel.setMethodCallHandler(this);
    methodChannel.setMethodCallHandler(::onMethodCall)
  }

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_play_asset")
      channel.setMethodCallHandler(FlutterPlayAssetPlugin())
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method=="get_asset"){
      getAbsoluteAssetPath(call.arguments.toString())
    } else if (call.method=="download_asset"){
      downloadPack(call.arguments.toString())
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    assetPackManager!!.cancel(Collections.singletonList(package_name))
    assetPackManager!!.unregisterListener(mAssetPackStateUpdateListener)
  }


  var mAssetPackStateUpdateListener: AssetPackStateUpdateListener = object : AssetPackStateUpdateListener {
    override fun onStateUpdate(state: AssetPackState) {
      var x = state.status()
      when(x){
        AssetPackStatus.CANCELED -> {
          Log.d("PUZZLE", "CANCELED")
        }
        AssetPackStatus.COMPLETED -> {
          Log.d("PUZZLE", "COMPLETED")
        }
        AssetPackStatus.DOWNLOADING -> methodChannel.invokeMethod(FLUTTER_METHOD_DOWNLOAD_PROGRESS_UPDATE, state.transferProgressPercentage())
        AssetPackStatus.FAILED ->  {
          Log.d("PUZZLE", "FAILED")
        }
        AssetPackStatus.NOT_INSTALLED ->  {
          Log.d("PUZZLE", "NOT_INSTALLED")
        }
        AssetPackStatus.PENDING ->  {
          Log.d("PUZZLE", "PENDING")
        }
        AssetPackStatus.TRANSFERRING ->  {
          Log.d("PUZZLE", "TRANSFERRING")
        }
        AssetPackStatus.UNKNOWN ->  {
          Log.d("PUZZLE", "UNKNOWN")
        }
        AssetPackStatus.WAITING_FOR_WIFI ->  {
          Log.d("PUZZLE", "WAITING_FOR_WIFI")
        }
        else ->  {
          Log.d("PUZZLE", state.status().toString())
        }
      }
    }
  }

  private fun getAbsoluteAssetPath(assetPack: String) {
    methodChannel.invokeMethod(FLUTTER_METHOD_PLAYASSET_DOWNLOAD, "Checking asset path...")
    val assetPackPath = assetPackManager!!.getPackLocation(assetPack)
    val assetsFolderPath = assetPackPath?.assetsPath()
    if (assetsFolderPath!=null){
      try {
        val file = File(assetsFolderPath)
        if (file.isDirectory) {
          methodChannel.invokeMethod(FLUTTER_METHOD_PLAYASSET_DOWNLOAD, assetsFolderPath)
        } else {
          methodChannel.invokeMethod(FLUTTER_METHOD_PLAYASSET_DOWNLOAD, "Error: " +assetsFolderPath+" not directory...")
        }
      } catch (e: Exception){
        methodChannel.invokeMethod(FLUTTER_METHOD_PLAYASSET_DOWNLOAD, "Error: " + e.message + "...")
      }
    } else {
      methodChannel.invokeMethod(FLUTTER_METHOD_PLAYASSET_DOWNLOAD, assetsFolderPath+" is null...")
      downloadPack(assetPack)
    }
  }

  private fun downloadPack(assetPack: String){
    package_name = assetPack
    methodChannel.invokeMethod(FLUTTER_METHOD_PLAYASSET_DOWNLOAD, "Start download pack "+ assetPack +"...")
    val list: MutableList<String> = ArrayList()
    list.add(assetPack)
    assetPackManager!!.fetch(list).addOnSuccessListener {
      methodChannel.invokeMethod(FLUTTER_METHOD_PLAYASSET_DOWNLOAD, "Success download pack "+ assetPack +"...")
      getAbsoluteAssetPath(assetPack)
    }.addOnFailureListener {
      methodChannel.invokeMethod(FLUTTER_METHOD_PLAYASSET_DOWNLOAD, "Failed download pack "+ assetPack +"...")
    }
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    ctx = binding.activity.applicationContext
    assetPackManager = AssetPackManagerFactory.getInstance(ctx)
    assetPackManager!!.registerListener(mAssetPackStateUpdateListener)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }
}
