//
//  File.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  Copyright © 2018 dflab. All rights reserved.
//

import Foundation

class PTData {
    var frames = [Data]()

    private(set) var pm1_0 : UInt16 = 0
    private(set) var pm2_5 : UInt16 = 0
    private(set) var pm10  : UInt16 = 0
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

        let ints = d.withUnsafeBytes { (int16Ptr: UnsafePointer<UInt16>) -> [UInt16] in
            var x = [UInt16]()
            var i = 0

            while (i<=15) {
                let n = UInt16(bigEndian: int16Ptr[i])
                i += 1
                x.append(n)
            }

            return x
        }

        guard ints[0] == 16973, ints[1] == 28 else { // begin, length
            return false
        }

        let b = [UInt8](d)
        var sum : UInt16 = 0;
        var c = 0;

        for i in b {
            c+=1
            if (c>30) {
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

        return true
    }
}
