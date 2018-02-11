//
//  PTDeviceCallbacks.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  Copyright © 2018 dflab. All rights reserved.
//

import Foundation

typealias PTDeviceConnStateChangeCallback = (_ isConnected : Bool, _ err : Error?) -> Void
typealias PTDeviceDataCallback = (PTData?, Error?) -> Void
