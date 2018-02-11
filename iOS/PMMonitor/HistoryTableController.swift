//
//  HistoryTableController.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  Copyright © 2018 dflab. All rights reserved.
//

import UIKit

class HistoryTableController: NSObject, UITableViewDelegate, UITableViewDataSource {

    var dataLimit : Int = 10

    private(set) internal var data = [PTData]()
    private let tableView : UITableView

    init(tableView tView : UITableView) {
        tableView = tView
        super.init()
    }

    func add(_ d : PTData) {
        data.insert(d, at: 0)

        if data.count > dataLimit {
            data.removeLast()
        }

        tableView.reloadData()
    }

    private func string(forData i : Int) -> String {
        let d = data[i]
        let str = String(format: "PM1.0=%i, PM2.5=%i, PM10=%i",d.pm1_0, d.pm2_5, d.pm10)
        return str
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "dataCell", for: indexPath) as! HistoryCell

        let d = data[indexPath.row]

        cell.set(from: d)

        return cell
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 65
    }
}
