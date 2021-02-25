import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ttchat_flowball/ttchat_flowball.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool _isShowingWindow = false;
  bool _isUpdatedWindow = false;

  @override
  void initState() {
    super.initState();
    _initPlatformState();
    _requestPermissions();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> _initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await TtchatFlowball.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> _requestPermissions() async {
    await TtchatFlowball.requestPermissions;
  }

  void _showOverlayWindow() {
    if (!_isShowingWindow) {
      TtchatFlowball.showSystemWindow(
          height: 230,
          notificationTitle: "Incoming Call",
          notificationBody: "+1 646 980 4741");
      setState(() {
        _isShowingWindow = true;
      });
    } else if (!_isUpdatedWindow) {
      TtchatFlowball.updateSystemWindow(
          height: 230,
          notificationTitle: "Outgoing Call",
          notificationBody: "+1 646 980 4741");
      setState(() {
        _isUpdatedWindow = true;
      });
    } else {
      setState(() {
        _isShowingWindow = false;
        _isUpdatedWindow = false;
      });
      TtchatFlowball.closeSystemWindow();
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('System Alert Window Example App'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Text('Running on: $_platformVersion\n'),
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 8.0),
                child: MaterialButton(
                  onPressed: _showOverlayWindow,
                  textColor: Colors.white,
                  child: !_isShowingWindow
                      ? Text("Show system alert window")
                      : !_isUpdatedWindow
                          ? Text("Update system alert window")
                          : Text("Close system alert window"),
                  color: Colors.deepOrange,
                  padding: const EdgeInsets.symmetric(vertical: 8.0),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}

///
/// Whenever a button is clicked, this method will be invoked with a tag (As tag is unique for every button, it helps in identifying the button).
/// You can check for the tag value and perform the relevant action for the button click
///
void callBack(String tag) {
  print(tag);
  switch (tag) {
    case "simple_button":
    case "updated_simple_button":
      TtchatFlowball.closeSystemWindow();
      break;
    case "focus_button":
      print("Focus button has been called");
      break;
    default:
      print("OnClick event of $tag");
  }
}
