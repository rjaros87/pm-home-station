//
//  PTDeviceCallbacks.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  License: GPL 3.0
//

import Foundation

typealias PTDeviceConnStateChangeCallback = (_ isConnected : Bool, _ err : Error?) -> Void
typealias PTDeviceDataCallback = (PTData?, Error?) -> Void
