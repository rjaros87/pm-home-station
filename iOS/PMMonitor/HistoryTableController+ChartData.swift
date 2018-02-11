//
//  HistoryTableController+ChartData.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 11.02.2018.
//  License: GPL 3.0
//

import UIKit

extension HistoryTableController {

    func chartData() -> [PNLineChartData] {

        guard data.count > 2 else {
            return []
        }

        let pm10 = PNLineChartData()
        let pm2_5 = PNLineChartData()
        let pm1_0 = PNLineChartData()


        pm2_5.color = PNRed
        pm2_5.itemCount = data.count
        pm2_5.inflexPointStyle = .None
        pm2_5.getData = ({
            (index: Int) -> PNLineChartDataItem in
            let i = self.data.count - index - 1;
            let yValue = CGFloat(self.data[i].pm2_5)
            let item = PNLineChartDataItem(y: yValue)
            return item
        })

        pm1_0.color = PNGrey
        pm1_0.itemCount = data.count
        pm1_0.inflexPointStyle = .None
        pm1_0.getData = ({
            (index: Int) -> PNLineChartDataItem in
            let i = self.data.count - index - 1;
            let yValue = CGFloat(self.data[i].pm1_0)
            let item = PNLineChartDataItem(y: yValue)
            return item
        })

        pm10.color = PNBlue
        pm10.itemCount = data.count
        pm10.inflexPointStyle = .None
        pm10.getData = ({
            (index: Int) -> PNLineChartDataItem in
            let i = self.data.count - index - 1;
            let yValue = CGFloat(self.data[i].pm10)
            let item = PNLineChartDataItem(y: yValue)
            return item
        })

        return [pm1_0,pm2_5,pm10]
    }

    func chartXLabels() -> [String] {
        var arr = [String]()

        guard data.count > 2 else {
            return []
        }
/*
!!!! X-Labels temporary removed, as they're ugly when there are
         many points on graph.
 */

/*
        let shortDateFormatter = DateFormatter()
        shortDateFormatter.dateFormat = "mm:ss"
        shortDateFormatter.locale = Locale.current
*/
        for _ in stride(from: data.count-1, to: 0, by: -2) {
//            arr.append(shortDateFormatter.string(from: data[i].date))
            arr.append("")
        }

        return arr
    }
}
