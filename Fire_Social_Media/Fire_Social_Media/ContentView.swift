import SwiftUI
import core
import AppKoin

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> some UIViewController {
        MainApplicationKt.MainApplicationMainAppViewController(context: "iOS" as NSString)
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}
