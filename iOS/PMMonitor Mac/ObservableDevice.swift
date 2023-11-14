//
//  PMMonitor Mac
//
//  Created by Jerzy Łukjaniec on 16.09.2022.
//  License: GPL 3.0
//

import SwiftUI

private struct DataFormat {
    static let Full = """
    PM 1.0     = %i μ/㎥       H₂CO       = %.2f mg/㎥
    PM 2.5     = %i μ/㎥       Temp.      = %.2f °C
    PM 10      = %i μ/㎥       Humidity   = %.2f %
    """

    static let Short = """
    PM 1.0  = %i μ/㎥
    PM 2.5 = %i μ/㎥
    PM 10  = %i μ/㎥
    """
}

class ObservableDevice: ObservableObject {
    @Published var message = ""
    @Published var data = [ChartDataPoint]()

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
            if let data {
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
                             data.pm1_0,  formaldehyde,
                             data.pm2_5,  data.temperature ?? "UNKNOWN",
                             data.pm10,   data.humidity ?? "UNKNOWN"
            )
        } else {
            message = String(format: DataFormat.Short,
                             data.pm1_0,
                             data.pm2_5,
                             data.pm10
            )
        }
        self.data.append(contentsOf: data.chartData)
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

    struct ChartDataPoint: Identifiable {
        let id = UUID()
        let name: String
        let date: Date
        let value: Float
    }
}

fileprivate extension PTData {
    var chartData: [ObservableDevice.ChartDataPoint] {
        var result = [ObservableDevice.ChartDataPoint]()

        result.append(.init(name: "PM 1.0", date: date, value: Float(pm1_0)))
        result.append(.init(name: "PM 2.5", date: date, value: Float(pm2_5)))

        if let formaldehyde {
            result.append(.init(name: "H₂CO", date: date, value: formaldehyde))
        }

        if let temperature {
            result.append(.init(name: "t", date: date, value: temperature))
        }

        if let humidity {
            result.append(.init(name: "RH", date: date, value: humidity))
        }

        return result
    }
}
