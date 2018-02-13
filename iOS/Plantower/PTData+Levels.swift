//
//  PTData+Levels.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 11.02.2018.
//  License: GPL 3.0
//

import Foundation

// Levels taken from AQIColor Android implementation
extension PTData {
    enum PollutionLevel {
        case VeryGood
        case Good
        case Moderate
        case Satisfactory
        case Bad
        case Hazardous
    }

    var pm2_5level : PollutionLevel {
        get {
            return PTData.levelFor(pm2_5: pm2_5)
        }
    }

    var pm10level : PollutionLevel {
        get {
            return PTData.levelFor(pm10: pm10)
        }
    }

    private class func levelFor(pm10 amt: UInt16) -> PollutionLevel {
        switch amt {
        case 0..<20:
            return .VeryGood
        case 20..<60:
            return .Good
        case 60..<100:
            return .Moderate
        case 100..<140:
            return .Satisfactory
        case 140..<200:
            return .Bad
        default:
            return .Hazardous
        }
    }

    private class func levelFor(pm2_5 amt: UInt16) -> PollutionLevel {
        switch amt {
        case 0...12:
            return .VeryGood
        case 12...36:
            return .Good
        case 36...60:
            return .Moderate
        case 60...84:
            return .Satisfactory
        case 84...120:
            return .Bad
        default:
            return .Hazardous
        }
    }
}
