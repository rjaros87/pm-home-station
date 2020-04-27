//
//  InterfaceController.swift
//  PMWatch Extension
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  License: GPL 3.0
//

import WatchKit
import Foundation

class InterfaceController: WKInterfaceController {

    @IBOutlet var table : WKInterfaceTable!

    private let device = PTDevice()

    override func willActivate() {
        start()
        super.willActivate()
    }
    
    override func didDeactivate() {
        stop()
        super.didDeactivate()
    }

    private func start() {
        table.setNumberOfRows(1, withRowType: "waitingCell")

        let dataCallback : PTDeviceDataCallback =  { (data, error) in
            if let data = data {
                self.presentData(data)
            }
        }

        device.connect { (isConnected, err) in
            if (isConnected) {
                self.device.start(dataCallback)
            } else {
                let cellID : String
                if let err = err as? PTDeviceError {
                    switch (err) {
                    case .BTPoweredOff:
                        cellID = "turnOnBTCell"
                    case .BTFailure:
                        cellID = "unavailableBTCell"
                    case .connection(_):
                        cellID = "connErrorCell"
                    }
                } else {
                    cellID = "waitingCell"
                }
                DispatchQueue.main.async {
                    self.table.setNumberOfRows(1, withRowType: cellID)
                }
            }
        }
    }

    private func stop() {
        table.setNumberOfRows(1, withRowType: "waitingCell")
        device.disconnect()
    }

    private func presentData(_ data : PTData) {
        guard Thread.isMainThread else {
            DispatchQueue.main.async {
                self.presentData(data)
            }
            return
        }

        let numberOfRows: Int
        if data.formaldehyde != nil {
            numberOfRows = 6
        } else {
            numberOfRows = 3
        }

        if self.table.numberOfRows != numberOfRows {
            self.table.setNumberOfRows(numberOfRows, withRowType: "dataCell")
        }

        row(0).set(amount: data.pm1_0, for: "PM 1.0")
        row(1).set(amount: data.pm2_5, for: "PM 2.5")
        row(2).set(amount: data.pm10,  for: "PM 10")

        if numberOfRows == 6 {
            row(3).set(amount: data.formaldehyde, for: "H₂CO")
            row(4).set(amount: data.temperature,  for: "Temp. (°C)")
            row(5).set(amount: data.humidity,  for: "Humid. (%)")
        }
    }

    func row(_ index: Int) -> DataRow {
        return self.table.rowController(at: index) as! DataRow
    }
}
