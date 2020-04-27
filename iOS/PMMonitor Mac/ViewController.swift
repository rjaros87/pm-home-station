//
//  ViewController.swift
//  PMMonitor Mac
//
//  Created by Jerzy Łukjaniec on 16.02.2018.
//  License: GPL 3.0
//

import Cocoa

class ViewController: NSViewController {
    @IBOutlet var output : NSTextField!

    let device = PTDevice()

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override var representedObject: Any? {
        didSet {
        // Update the view, if already loaded.
        }
    }

    override func viewWillAppear() {
        device.connect { (connected, err) in
            if (connected) {
                self.device.start({ (data, err) in
                    if let data = data {
                        self.setOutput(data: data)
                    } else if let err = err {
                        self.setOutput(string: err.localizedDescription)
                    }
                })
            } else {
                if let err = err {
                    if let err = err as? PTDeviceError {
                        switch err {
                        case .BTPoweredOff:
                            self.setOutput(string: "Turn On Bluetooth")
                        case .BTFailure:
                            self.setOutput(string: "BT LE Not supported?")
                        case .connection(let c):
                            self.setOutput(string: c)
                        }
                    } else {
                        self.setOutput(string: err.localizedDescription)
                    }
                } else {
                    self.setOutput(string: "end.")
                }
            }
        }
    }

    private func setOutput(string : String) {
        guard Thread.current.isMainThread else {
            DispatchQueue.main.async {
                self.setOutput(string: string)
            }
            return
        }

        output.stringValue = string
    }

    private func setOutput(data : PTData) {
        if data.formaldehyde != nil {
            let formatString = "PM 1.0     = %i μ/㎥\nPM 2.5     = %i μ/㎥\nPM 10      = %i μ/㎥\n\nH₂CO       = %.2f mg/㎥\nTemp.      = %.2f °C\nHumidity = %.2f %"
            let str = String(format: formatString, data.pm1_0, data.pm2_5, data.pm10, data.formaldehyde ?? "UNKNOWN", data.temperature ?? "UNKNOWN", data.humidity ?? "UNKNOWN")
            setOutput(string: str)
        } else {
            let str = String(format: "PM 1.0  = %i μ/㎥\nPM 2.5 = %i μ/㎥\nPM 10  = %i μ/㎥",data.pm1_0, data.pm2_5, data.pm10)
            setOutput(string: str)
        }
    }


}

