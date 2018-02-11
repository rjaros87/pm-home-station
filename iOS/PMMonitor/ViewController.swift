//
//  ViewController.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 09.02.2018.
//  Copyright © 2018 dflab. All rights reserved.
//

import UIKit
import CoreBluetooth

class ViewController: UIViewController {

    @IBOutlet var statusLabel : UILabel!

    @IBOutlet var pm10title : UILabel!
    @IBOutlet var pm1_0title : UILabel!
    @IBOutlet var pm2_5title : UILabel!

    @IBOutlet var tView: UITableView!

    @IBOutlet var chartView : UIView!
    var lineChart : PNLineChart?

    var history : HistoryTableController?

    let device = PTDevice()

    var supressDialogs = false

    override func viewDidLoad() {
        super.viewDidLoad()

        let nCenter = NotificationCenter.default

        nCenter.addObserver(forName: .UIApplicationWillResignActive, object: nil, queue: nil) { (_) in
            self.supressDialogs = true
            self.device.disconnect()
        }
        nCenter.addObserver(forName: .UIApplicationDidBecomeActive, object: nil, queue: nil) { (_) in
            self.start()
        }

        setupChart()
    }

    override func viewDidAppear(_ animated: Bool) {

        if history == nil {
            let h = HistoryTableController(tableView: tView)
            tView.delegate = h
            tView.dataSource = h
            history = h
        }
    }

    func setupChart() {
        pm10title.textColor = PNBlue
        pm1_0title.textColor = PNGrey
        pm2_5title.textColor = PNRed

        let chart = PNLineChart(frame: chartView.bounds)
        chart.yLabelFormat = "%1.1f"
        chart.showLabel = true
        chart.backgroundColor = UIColor.clear
        chart.showCoordinateAxis = true
        chart.center = chartView.center
        chartView.addSubview(chart)
        lineChart = chart
    }

    func start() {
        supressDialogs = false
        let dataCallback : PTDeviceDataCallback =  { (data, error) in
            if let data = data {
                DispatchQueue.main.async {
                    guard let history = self.history else {
                        return
                    }
                    history.add(data)
                    self.lineChart?.xLabels = history.chartXLabels() as NSArray
                    self.lineChart?.chartData = history.chartData() as NSArray
                    self.lineChart?.strokeChart()
                }
            }

        }

        device.connect { (isConnected, err) in

            let statusString : String
            let statusColour : UIColor

            if (isConnected) {
                statusString = "Connected"
                statusColour = UIColor.green
                self.device.start(dataCallback)
            } else {

                statusColour = UIColor.red

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
                    statusString = "Disconnected"
                }
            }
            DispatchQueue.main.async {
                self.statusLabel.text = statusString
                self.statusLabel.textColor = statusColour
                if (!isConnected && !self.supressDialogs) {
                    self.showDisconnectedDialog()
                    self.supressDialogs = false
                }
            }
        }
    }

    func showDisconnectedDialog() {
        let ctrl = UIAlertController(title: "Disconnected", message: "Start to search for devices?", preferredStyle: UIAlertControllerStyle.alert)

        let scanAction = UIAlertAction(title: "Search", style: .default) { (_) in
            self.statusLabel.text = "Searching..."
            self.statusLabel.textColor = UIColor.gray
            self.start()
        }

        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)

        ctrl.addAction(scanAction)
        ctrl.addAction(cancelAction)

        self.present(ctrl, animated: true, completion: nil)
    }

}
