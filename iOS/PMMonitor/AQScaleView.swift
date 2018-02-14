//
//  AQScaleView.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 14.02.2018.
//  License: GPL 3.0
//

import UIKit

class AQScaleView: UIView {

    @IBOutlet var v1 : UIView!
    @IBOutlet var v2 : UIView!
    @IBOutlet var v3 : UIView!
    @IBOutlet var v4 : UIView!
    @IBOutlet var v5 : UIView!
    @IBOutlet var v6 : UIView!

    @IBOutlet var unitLbl : UILabel!
    @IBOutlet var amtLbl  : UILabel!

    var views = [UIView]()
    var colors = [UIColor]()

    func prepare() {
        guard views.count == 0 else {
            return
        }

        amtLbl.text = " "

        views = [v1,v2,v3,v4,v5,v6]

        for i in 0..<views.count {
            colors.append(views[i].backgroundColor!)
            views[i].backgroundColor = UIColor.gray
        }
    }

    func setUnit(unit : String) {
        unitLbl.text = unit
    }

    func setAirQuality(level lvl: PTData.PollutionLevel, pmAmount amt : UInt16) {
        let str = String(format:"%i μ/㎥", amt)
        amtLbl.text = str

        for i in 0...lvl.rawValue {
            views[i].backgroundColor = colors[i]
        }

        let maxLvl = PTData.PollutionLevel.Hazardous.rawValue
        if lvl.rawValue <  maxLvl {
            for j in (lvl.rawValue+1)...maxLvl {
                views[j].backgroundColor = UIColor.gray
            }
        }
    }

    func color(forLevel lvl: PTData.PollutionLevel) -> UIColor {
        return colors[lvl.rawValue]
    }
}
