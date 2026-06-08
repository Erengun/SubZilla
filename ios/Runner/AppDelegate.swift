import Flutter
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate {
  private var liveActivityChannel: AnyObject?

  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
    if #available(iOS 16.2, *),
       let registrar = self.registrar(forPlugin: "LiveActivityChannel") {
      liveActivityChannel = LiveActivityChannel(binaryMessenger: registrar.messenger())
    }
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
