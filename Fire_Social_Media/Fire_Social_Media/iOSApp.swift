import SwiftUI
import core
import AppKoin

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        // Start Koin for iOS
        AppKoinInitKt.doInitAllKoin(platformContext: IosPlatformContext())
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

