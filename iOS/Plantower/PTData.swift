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
        var d = Data()

        for frame in frames {
            d.append(frame)
        }

        guard d.count == 32 || d.count == 40 else {
            return false
        }

        let ints: [UInt16] = makeArray(from: d)

        guard ints[0] == 16973, (ints[1] == 28 || ints[1] == 36 ) else { // begin, length
            return false
        }

        let b = [UInt8](d)
        var sum : UInt16 = 0;
        var c = 0;

        for i in b {
            c+=1
            if (c>2+ints[1]) {
                break
            }
            sum += UInt16(i)
        }

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
        let byteLength = T.bitWidth / 8
        return data.withUnsafeBytes { (ptr: UnsafePointer<T>) -> [T] in
            var x = [T]()

            for i in 0...((data.count-1)/byteLength) {
                let n = T(bigEndian: ptr[i])
                x.append(n)
            }

            return x
        }
    }

    private func signedInt(from data: Data, offset: Int) -> Int16? {
        guard data.count>offset+2 else {
            return nil
        }
        let truncatedData = data.subdata(in: offset..<offset+2)
        let ints: [Int16] = makeArray(from: truncatedData)
        return ints.first
    }
}
