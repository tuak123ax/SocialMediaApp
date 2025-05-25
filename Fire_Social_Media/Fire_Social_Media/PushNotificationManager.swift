import UIKit
import Firebase
import FirebaseMessaging
import UserNotifications
import shared

class PushNotificationManager: NSObject, UNUserNotificationCenterDelegate, MessagingDelegate {
    
    static let shared = PushNotificationManager()

    func registerForPushNotifications() {
        UNUserNotificationCenter.current().delegate = self

        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            if granted {
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
        }

        Messaging.messaging().delegate = self
    }

    // Called when FCM receives a new token
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("FCM Token: \(fcmToken ?? "")")
        // Here you can pass the token to shared Kotlin code if needed
    }

    // Handle incoming notification when app is in foreground
    func userNotificationCenter(_ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {

        let userInfo = notification.request.content.userInfo
        print("Foreground push: \(userInfo)")
        completionHandler([.badge, .sound, .banner, .list])
    }
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo

        if let payload = userInfo as? [String: Any] {
            // Convert to Kotlin dictionary
            SharedPushHandler.shared.handlePushNotification(payload: payload)
        }

        completionHandler()
    }
}
