//
//  PMMonitor Mac
//
//  Created by Jerzy Łukjaniec on 16.09.2022.
//  License: GPL 3.0
//

import SwiftUI

struct MainView: View {
    @ObservedObject
    private var device = ObservableDevice()

    var body: some View {
        VStack {
            Image(systemName: "antenna.radiowaves.left.and.right")
                .imageScale(.large)
                .foregroundColor(.accentColor)
                .padding(.bottom, 20)
            Text(device.message)
        }
        .padding()
    }
}
