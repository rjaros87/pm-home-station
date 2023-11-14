//
//  PMMonitor Mac
//
//  Created by Jerzy Łukjaniec on 16.09.2022.
//  License: GPL 3.0
//

import SwiftUI
import Charts

struct MainView: View {
    @ObservedObject
    private var device = ObservableDevice()

    @State
    private var selectedDate: Date?

    var body: some View {
        VStack {
            Image(systemName: "antenna.radiowaves.left.and.right")
                .imageScale(.large)
                .foregroundColor(.accentColor)
                .padding(.bottom, 20)
            Text(device.message)
            chart
        }
        .padding()
    }

    private var chart: some View {
        Chart {
            ForEach(device.data) {
                LineMark(
                    x: .value("Date", $0.date),
                    y: .value($0.name, $0.value)
                ).foregroundStyle(by: .value("Name", $0.name))
            }
            if let nearestAvailableDate {
                RectangleMark(x: .value("Date", nearestAvailableDate), width: .fixed(2))
                    .foregroundStyle(.primary.opacity(0.2))
                    .annotation(position: annotationPosition, alignment: .center, spacing: 0) {
                        let filteredData = device.data.filter {
                            abs($0.date.timeIntervalSince(nearestAvailableDate)).isLess(than: 1)
                        }
                        if !filteredData.isEmpty {
                            ChartAnnotation(
                                data: filteredData
                            )
                        }
                    }
            }
        }.chartOverlay { proxy in
            Color.clear
                .onContinuousHover(coordinateSpace: .local) { hoverPhase in
                    switch hoverPhase {
                    case .active(let cGPoint):
                        selectedDate = proxy.value(atX: cGPoint.x)
                    case .ended:
                        selectedDate = nil
                    }
                }
        }
    }

    private var annotationPosition: AnnotationPosition {
        guard let selectedDate else  {
            return .trailing
        }

        let middlePoint = device.data.count / 2
        let middleDate = device.data[middlePoint].date

        return selectedDate.compare(middleDate) == .orderedDescending ? .leading : .trailing
    }

    private var nearestAvailableDate: Date? {
        guard let selectedDate else {
            return nil
        }
        return device.data.map(\.date).sorted().first {
            $0.timeIntervalSince(selectedDate) >= 0
        }
    }

    struct ChartAnnotation: View {
        let data: [ObservableDevice.ChartDataPoint]

        var body: some View {
            VStack(alignment: .leading) {
                Text(data.first?.date.formatted() ?? " ")
                    .font(.headline)
                Divider()
                ForEach(data) {
                    Text("\($0.name): \($0.value.formatted(.number.precision(.fractionLength(..<3))))")
                }
            }
            .padding()
            .background(Color(nsColor: .controlBackgroundColor))
        }
    }
}
