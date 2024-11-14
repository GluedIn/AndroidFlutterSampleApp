import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(SampleApp());
}

class SampleApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GluedIn',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        appBar: AppBar(
          centerTitle: true,
          title: const Text("GluedIn Sample App"),
        ),
        body: Center(
          child: HomeWidget(),
        ),
      ),
      debugShowCheckedModeBanner: false,
    );
  }
}

class HomeWidget extends StatefulWidget {
  @override
  _HomeWidgetState createState() => _HomeWidgetState();
}

class _HomeWidgetState extends State<HomeWidget> {
  static const platform = MethodChannel("com.gluedin.io/gluedinsdk");

  void _launchGluedInApp() async {
    try {
      platform.invokeMethod("launchGluedInApp");
    } on PlatformException catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: <Widget>[
        // IconButton(onPressed: _launchGluedInApp, icon: const Icon(Icons.face))
        ElevatedButton(
          child: const Text("Launch GluedIn App"),
          onPressed: _launchGluedInApp,
        )
      ],
    );
  }
}
