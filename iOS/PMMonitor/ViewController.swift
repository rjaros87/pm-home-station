//
//  ViewController.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 09.02.2018.
//  License: GPL 3.0
//

import UIKit
import CoreBluetooth
import Charts

class ViewController: UIViewController {

    @IBOutlet var mainLabel   : UILabel!
    @IBOutlet var mainSubLabel: UILabel!

    @IBOutlet var mainColorView : UIView!

    @IBOutlet var pm1_0scale : AQScaleView!
    @IBOutlet var pm2_5scale : AQScaleView!
    @IBOutlet var pm10scale  : AQScaleView!

    @IBOutlet var lineChart  : LineChartView!

    let device = PTDevice()

    var supressDialogs = false

    override func viewDidLoad() {
        super.viewDidLoad()

        let nCenter = NotificationCenter.default

        nCenter.addObserver(forName: UIApplication.willResignActiveNotification, object: nil, queue: nil) { (_) in
            self.supressDialogs = true
            self.device.disconnect()
        }
        nCenter.addObserver(forName: UIApplication.didBecomeActiveNotification, object: nil, queue: nil) { (_) in
            self.start()
        }

        self.mainLabel.text = "Disconnected"
        self.mainSubLabel.text = "Searching ..."
        self.mainColorView.backgroundColor = UIColor.gray

        pm1_0scale.setUnit(unit: "PM 1.0")
        pm1_0scale.prepare()

        pm2_5scale.setUnit(unit: "PM 2.5")
        pm2_5scale.prepare()

        pm10scale.setUnit(unit: "PM 10")
        pm10scale.prepare()

        setupChart()
    }

    func start() {
        supressDialogs = false
        let dataCallback : PTDeviceDataCallback =  { (data, error) in
            if let data = data {
                self.addToChart(data)
                let cData = self.chartData()
                DispatchQueue.main.async {
                    self.mainLabel.text = data.overallLevel.name
                    self.mainColorView.backgroundColor = self.pm1_0scale.color(forLevel: data.overallLevel)
                    self.mainSubLabel.text = "air quality"

                    self.pm1_0scale.setAirQuality(level: data.pm1_0level, pmAmount: data.pm1_0)
                    self.pm2_5scale.setAirQuality(level: data.pm2_5level, pmAmount: data.pm2_5)
                    self.pm10scale.setAirQuality(level: data.pm10level, pmAmount: data.pm10)

                    self.lineChart.data = cData
                }
            }

        }

        device.connect { (isConnected, err) in
            if (isConnected) {
                self.device.start(dataCallback)
            } else {
                let statusString : String
                if let err = err as? PTDeviceError {
                    self.supressDialogs = true
                    switch (err) {
                    case .BTPoweredOff:
                        statusString = "Turn on Bluetooth"
                    case .BTFailure:
                        statusString = "Bluetooth unavailable"
                    case .connection(let str):
                        statusString = str
                    }
                } else {
                    statusString = " "
                }
                DispatchQueue.main.async {
                    self.mainLabel.text = "Disconnected"
                    self.mainSubLabel.text = statusString
                    self.mainColorView.backgroundColor = UIColor.gray
                    if (!self.supressDialogs) {
                        self.showDisconnectedDialog()
                        self.supressDialogs = false
                    }
                }
            }

        }
    }

    func showDisconnectedDialog() {
        let ctrl = UIAlertController(title: "Disconnected", message: "Start to search for devices?", preferredStyle: .alert)

        let scanAction = UIAlertAction(title: "Search", style: .default) { (_) in
            self.mainLabel.text = "Disconnected"
            self.mainSubLabel.text = "Searching ..."
            self.mainColorView.backgroundColor = UIColor.gray
            self.start()
        }

        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)

        ctrl.addAction(scanAction)
        ctrl.addAction(cancelAction)

        self.present(ctrl, animated: true, completion: nil)
    }
}
