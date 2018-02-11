//
//  DataRow.swift
//  PMWatch Extension
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  License: GPL 3.0
//

import WatchKit

enum PSize : String{
    case pm1_0 = "PM 1.0"
    case pm2_5 = "PM 2.5"
    case pm10  = "PM 10"
}

class DataRow: NSObject {
    @IBOutlet var unitLabel : WKInterfaceLabel!
    @IBOutlet var amountLabel : WKInterfaceLabel!

    func set(from data: PTData, forPSize size: PSize) {
        unitLabel.setText(size.rawValue)

        let amt : UInt16

        switch size {
        case .pm1_0:
            amt = data.pm1_0
        case .pm2_5:
            amt = data.pm2_5
        case .pm10:
            amt = data.pm10
        }

        let str = String(format:"%i",amt)

        amountLabel.setText(str)


    }
}
