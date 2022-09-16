//
//  PMMonitor Mac
//
//  Created by Jerzy Łukjaniec on 16.09.2022.
//  License: GPL 3.0
//

import SwiftUI

private struct DataFormat {
    static let Full = """
    PM 1.0     = %i μ/㎥
    PM 2.5     = %i μ/㎥
    PM 10      = %i μ/㎥

    H₂CO       = %.2f mg/㎥
    Temp.      = %.2f °C
    Humidity = %.2f %
    """

    static let Short = """
    PM 1.0  = %i μ/㎥
    PM 2.5 = %i μ/㎥
    PM 10  = %i μ/㎥
    """
}

class ObservableDevice: ObservableObject {
    @Published var message = ""

    private let device: PTDevice

    init() {
        device = PTDevice()
        connect()
    }

    private func connect() {
        message = "Connecting..."
        device.connect { [weak self] isConnected, err in
            isConnected ? self?.start() : self?.showError(err)
        }
    }

    private func scheduleRetry() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 5, execute: {
            self.connect()
        })
    }

    private func start() {
        device.start { [weak self] data, error in
            if let data = data {
                self?.showData(data)
            } else {
                self?.showError(error)
            }
        }
    }

    private func showData(_ data: PTData) {
        guard Thread.isMainThread else {
            DispatchQueue.main.async {
                self.showData(data)
            }
            return
        }
        if let formaldehyde = data.formaldehyde {
            message = String(format: DataFormat.Full,
                             data.pm1_0,
                             data.pm2_5,
                             data.pm10,
                             formaldehyde,
                             data.temperature ?? "UNKNOWN",
                             data.humidity ?? "UNKNOWN"
            )
        } else {
            message = String(format: DataFormat.Short,
                             data.pm1_0,
                             data.pm2_5,
                             data.pm10
            )
        }

    }

    private func showError(_ error: Error?) {
        guard Thread.isMainThread else {
            DispatchQueue.main.async {
                self.showError(error)
            }
            return
        }
        guard let error = error else {
            message = "Unknown error."
            return
        }

        guard let error = error as? PTDeviceError else {
            message = error.localizedDescription
            return
        }

        switch error {
        case .BTPoweredOff:
            message = "Please turn on Bluetooth."
        case .BTFailure:
            message = "BT LE not supported?"
        case .connection(let errorMessage):
            message = errorMessage
        }
    }
}
