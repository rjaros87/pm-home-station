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
    enum PollutionLevel : Int {
        case VeryGood     = 0
        case Good         = 1
        case Moderate     = 2
        case Satisfactory = 3
        case Bad          = 4
        case Hazardous    = 5

        var name : String {
            switch self {
            case .VeryGood:
                return "Very Good"
            case .Good:
                return "Good"
            case .Moderate:
                return "Moderate"
            case .Satisfactory:
                return "Satisfactory"
            case .Bad:
                return "Bad"
            case .Hazardous:
                return "Hazardous"
            }
        }
    }

    var pm1_0level : PollutionLevel {
        get {
            return PTData.levelFor(pm2_5: pm1_0)
        }
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

    var overallLevel : PollutionLevel {
        get {
            var arr = [pm1_0level, pm2_5level, pm10level]
            arr.sort { (a, b) -> Bool in
                return a.rawValue<b.rawValue
            }

            if let result = arr.last {
                return result
            } else {
                return .VeryGood
            }
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
