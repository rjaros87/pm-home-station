//
//  HistoryTableController.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  License: GPL 3.0
//

import UIKit

class HistoryTableController: NSObject, UITableViewDelegate, UITableViewDataSource {

    var dataLimit : Int = 30

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
