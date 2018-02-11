//
//  HistoryCell.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 10.02.2018.
//  License: GPL 3.0
//

import UIKit

class HistoryCell: UITableViewCell {

    @IBOutlet var pm10Label : UILabel!
    @IBOutlet var pm1_0Label : UILabel!
    @IBOutlet var pm2_5Label : UILabel!
    @IBOutlet var dateLabel : UILabel!

    let dateFormatter = DateFormatter()

    override func awakeFromNib() {
        super.awakeFromNib()
        pm10Label.textColor = PNBlue
        pm1_0Label.textColor = PNGrey
        pm2_5Label.textColor = PNRed

        dateFormatter.dateFormat = "dd MMM yyy hh:mm:ss"
        dateFormatter.locale = Locale.current
    }

    func set(from data : PTData) {
        pm10Label.text = String(format:"%i%@", data.pm10, emojiForLevel(data.pm10level))
        pm2_5Label.text = String(format:"%i%@", data.pm2_5, emojiForLevel(data.pm2_5level))
        pm1_0Label.text = String(format:"%i❓", data.pm1_0)
        dateLabel.text = dateFormatter.string(from: data.date)
        
    }

    private func emojiForLevel(_ level : PTData.PollutionLevel) -> String {
        switch level {
        case .VeryGood:
            return "😀"
        case .Good:
            return "🙂"
        case .Moderate:
            return "🤨"
        case .Satisfactory:
            return "😕"
        case .Bad:
            return "😷"
        case .Hazardous:
            return "🤢"
        }
    }

}
