import SwiftUI
import core

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        // Start Koin for iOS
        KoinInitializerKt.doInitKoin(platformContext: IosPlatformContext())
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

