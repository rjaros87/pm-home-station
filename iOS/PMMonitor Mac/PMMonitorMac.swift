//
//  PMMonitor Mac
//
//  Created by Jerzy Łukjaniec on 16.09.2022.
//  License: GPL 3.0
//

import SwiftUI

@main
struct PMMonitorMac: App {
    var body: some Scene {
        WindowGroup {
            MainView()
                .frame(width: 300.0, height: 200.0)
        }
    }
}
