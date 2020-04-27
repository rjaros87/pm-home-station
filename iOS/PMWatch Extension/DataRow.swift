//
//  DataRow.swift
//  PMWatch Extension
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  License: GPL 3.0
//

import WatchKit

class DataRow: NSObject {
    @IBOutlet var unitLabel : WKInterfaceLabel!
    @IBOutlet var amountLabel : WKInterfaceLabel!

    func set(amount: UInt16, for unit: String) {
        unitLabel.setText(unit)
        amountLabel.setText(String(format:"%i", amount))
    }

    func set(amount: Float?, for unit: String) {
        unitLabel.setText(unit)
        if let amount = amount {
            amountLabel.setText(String(format:"%0.2f", amount))
        } else {
            amountLabel.setText("??")
        }
    }
}
