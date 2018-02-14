//
//  HistoryTableController.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 15.02.2018.
//  License: GPL 3.0
//

import UIKit
import Charts

fileprivate let dataLimit = 30
fileprivate var chartRawData : [PTData] = [PTData]()

extension ViewController {

    func setupChart() {
        let xAxis = lineChart.xAxis
        xAxis.granularity = 15
        xAxis.valueFormatter = TimeValueFormatter()
        xAxis.labelPosition = .bottom

        lineChart.rightAxis.enabled = false
        lineChart.chartDescription?.enabled = false
        lineChart.gridBackgroundColor = NSUIColor.white
    }

    func addToChart(_ d : PTData) {
        chartRawData.append(d)

        if chartRawData.count > dataLimit {
            chartRawData.removeFirst()
        }
    }

    func chartData() -> LineChartData {
        var d1 = [ChartDataEntry]()
        var d2 = [ChartDataEntry]()
        var d3 = [ChartDataEntry]()

        for d in chartRawData {
            let x = Double(d.date.timeIntervalSince1970)

            let e1 = ChartDataEntry(x: x, y: Double(d.pm1_0))
            let e2 = ChartDataEntry(x: x, y: Double(d.pm2_5))
            let e3 = ChartDataEntry(x: x, y: Double(d.pm10))

            d1.append(e1)
            d2.append(e2)
            d3.append(e3)
        }

        let ds1 = LineChartDataSet(values: d1, label: "PM 1.0")
        let ds2 = LineChartDataSet(values: d2, label: "PM 2.5")
        let ds3 = LineChartDataSet(values: d3, label: "PM 10")

        ds1.setColor(NSUIColor.orange)
        ds2.setColor(NSUIColor.purple)
        ds3.setColor(NSUIColor.red)

        setupDataSet(ds1)
        setupDataSet(ds2)
        setupDataSet(ds3)

        return LineChartData(dataSets: [ds1, ds2, ds3])
    }

    private func setupDataSet(_ ds : LineChartDataSet) {
        ds.drawCircleHoleEnabled = false
        ds.drawValuesEnabled = false
        ds.circleRadius = 2
        ds.setCircleColor(NSUIColor.gray)
    }
}

public class TimeValueFormatter: NSObject, IAxisValueFormatter {
    private let dateFormatter = DateFormatter()

    override init() {
        super.init()
        dateFormatter.dateFormat = "HH:mm:ss"
    }

    public func stringForValue(_ value: Double, axis: AxisBase?) -> String {
        return dateFormatter.string(from: Date(timeIntervalSince1970: value))
    }
}
