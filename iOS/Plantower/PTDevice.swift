//
//  PTDevice.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  License: GPL 3.0
//

import Foundation
import CoreBluetooth

enum PTDeviceError : Error {
    case connection(_ : String)
    case BTPoweredOff
    case BTFailure
}

class PTDevice : NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    private let deviceName = "HC-08"
    private let serviceUUID = CBUUID(string: "FFE0")
    private let characteristicUUID = CBUUID(string: "FFE1")

    private var cm : CBCentralManager?
    private var hc08 : CBPeripheral?
    private var service: CBService?
    private var dataCharacteristic : CBCharacteristic?

    private var data = PTData()

    private var connStateChanged : PTDeviceConnStateChangeCallback?
    private var dataCallback : PTDeviceDataCallback?

    ///

    public func connect(_ callback : @escaping PTDeviceConnStateChangeCallback) {
        connStateChanged = callback
        cm = CBCentralManager(delegate: self, queue: DispatchQueue.global(qos: .background))
    }

    public func start(_ callback : @escaping PTDeviceDataCallback) {
        guard let ch = dataCharacteristic,
              let peripheral = hc08 else {
            return
        }

        dataCallback = callback
        peripheral.readValue(for: ch)
        peripheral.setNotifyValue(true, for: ch)
    }

    public func disconnect() {

        if let cm = cm {
            if #available(watchOS 4.0 , iOS 10.0 ,OSX 10.13, *) {
                if (cm.isScanning) {
                    cm.stopScan()
                }
            } else {
                if hc08 == nil {
                    cm.stopScan()
                }
            }

            if let hc08 = hc08 {
                cm.cancelPeripheralConnection(hc08)
            }
        }

        hc08 = nil
        service = nil
        dataCharacteristic = nil
        data = PTData()
        dataCallback = nil
    }

    ///

    internal func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if (central.state == .poweredOn){
            central.scanForPeripherals(withServices: nil, options: nil)
        }
        else {
            let err : PTDeviceError
            if (central.state == .poweredOff) {
                err = PTDeviceError.BTPoweredOff
            } else {
                err = PTDeviceError.BTFailure
            }

            if let callback = connStateChanged {
                callback(false, err)
            }
        }
    }

    internal func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {

        guard let name = peripheral.name, name == deviceName else {
            return
        }

        cm?.stopScan()
        hc08=peripheral
        cm?.connect(peripheral, options: nil)
    }

    internal func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {

        peripheral.delegate = self
        peripheral.discoverServices([serviceUUID])
        hc08 = peripheral
    }

    internal func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        if let callback = connStateChanged {
            callback(false, error)
        }

    }

    internal func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        if let callback = connStateChanged {
            callback(false, error)
            connStateChanged = nil
        }

    }

    internal func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {

        guard let svc = peripheral.services?.first else {
            if let callback = connStateChanged {
                let err = PTDeviceError.connection("Service not found on connected device.")
                callback(false, err)
            }
            return
        }

        service = svc
        peripheral.discoverCharacteristics([characteristicUUID], for: svc)
    }

    internal func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        guard let ch = service.characteristics?.first else {
            if let callback = connStateChanged {
                let err = PTDeviceError.connection("Characteristic not found on connected device.")
                callback(false, err)
            }
            return
        }

        dataCharacteristic = ch
        if let callback = connStateChanged {
            callback(true, nil)
        }
    }

    internal func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {

        if let val = characteristic.value {
            if (!data.appendData(val)) {
                let d = data
                data = PTData()

                if (d.parse()) {
                    if let callback = dataCallback {
                        callback(d, nil)
                    }
                }
            }
        }
    }
    
}
