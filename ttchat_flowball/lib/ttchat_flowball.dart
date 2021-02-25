import 'dart:async';

import 'package:flutter/services.dart';

class TtchatFlowball {
  static const MethodChannel _channel = const MethodChannel('ttchat_flowball');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> get checkPermissions async {
    return await _channel.invokeMethod('checkPermissions');
  }

  static Future<bool> get requestPermissions async {
    return await _channel.invokeMethod('requestPermissions');
  }

  static Future<bool> showSystemWindow(
      {int width,
      int height,
      String notificationTitle = "Title",
      String notificationBody = "Body"}) async {
    final Map<String, dynamic> params = <String, dynamic>{};
    return await _channel.invokeMethod(
        'showSystemWindow', [notificationTitle, notificationBody, params]);
  }

  static Future<bool> updateSystemWindow(
      {int width,
      int height,
      String notificationTitle = "Title",
      String notificationBody = "Body"}) async {
    final Map<String, dynamic> params = <String, dynamic>{};
    return await _channel.invokeMethod(
        'updateSystemWindow', [notificationTitle, notificationBody, params]);
  }

  static Future<bool> closeSystemWindow() async {
    return await _channel.invokeMethod('closeSystemWindow');
  }
}
