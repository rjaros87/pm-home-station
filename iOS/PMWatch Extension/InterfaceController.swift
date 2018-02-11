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

        if self.table.numberOfRows != 3 {
            self.table.setNumberOfRows(3, withRowType: "dataCell")
        }

        let row0 = self.table.rowController(at: 0) as! DataRow
        let row1 = self.table.rowController(at: 1) as! DataRow
        let row2 = self.table.rowController(at: 2) as! DataRow

        row0.set(from: data, forPSize: PSize.pm1_0)
        row1.set(from: data, forPSize: PSize.pm2_5)
        row2.set(from: data, forPSize: PSize.pm10)
    }
}
