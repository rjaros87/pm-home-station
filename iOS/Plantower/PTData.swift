//
//  PTData.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  License: GPL 3.0
//

import Foundation

class PTData {
    private var frames = [Data]()

    private(set) var pm1_0 : UInt16 = 0
    private(set) var pm2_5 : UInt16 = 0
    private(set) var pm10  : UInt16 = 0

    private(set) var formaldehyde : Float? = nil
    private(set) var temperature  : Float? = nil
    private(set) var humidity     : Float? = nil

    private(set) var date  : Date = Date.distantPast

    func appendData(_ input: Data) -> Bool {
        if (frames.isEmpty) {
            guard (input[0] == 66) || frames.isEmpty else {
                return true
            }
        }

        guard frames.count<2 else {
            date = Date()
            return false
        }

        frames.append(input)

        return true
    }

    func parse() -> Bool {
        let d = frames.reduce(into: Data()) { $0.append($1) }
        guard d.count == 32 || d.count == 40 else {
            return false
        }

        let ints: [UInt16] = makeArray(from: d)

        guard ints[0] == 16973, (ints[1] == 28 || ints[1] == 36 ) else { // begin, length
            return false
        }

        let arrayToSum = d.dropLast(d.count - (2+Int(ints[1])))
        let sum = arrayToSum.reduce(0) { $0+UInt16($1) }

        guard sum == ints.last else {
            return false
        }

        pm1_0 = ints[5]
        pm2_5 = ints[6]
        pm10  = ints[7]

        if (ints[1] == 36) {
            formaldehyde = makeFloat(from: ints[14]) / 1000
            if let rawTemperature = signedInt(from: d, offset: 30) {
                temperature = makeFloat(from: rawTemperature) / 10.0
            }
            humidity = makeFloat(from: ints[16]) / 10.0
        }

        return true
    }

    private func makeFloat<T: FixedWidthInteger>(from int: T) -> Float {
        return Float(integerLiteral: Int64(int))
    }

    private func makeArray<T: FixedWidthInteger>(from data: Data) -> [T] {
        return data.withUnsafeBytes {
            $0.bindMemory(to: T.self).map { T(bigEndian: $0) }
        }
    }

    private func signedInt(from data: Data, offset: Int) -> Int16? {
        guard data.count>offset+2 else {
            return nil
        }
        return makeArray(from: data.subdata(in: offset..<offset+2)).first
    }
}
