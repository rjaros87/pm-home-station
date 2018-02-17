//
//  AppDelegate.swift
//  PMMonitor Mac
//
//  Created by Jerzy Łukjaniec on 16.02.2018.
//  License: GPL 3.0
//

import Cocoa

@NSApplicationMain
class AppDelegate: NSObject, NSApplicationDelegate {

    @IBOutlet var statusBarMenu : NSMenu!
    let statusItem = NSStatusBar.system.statusItem(withLength: NSStatusItem.variableLength)

    func applicationDidFinishLaunching(_ aNotification: Notification) {
        // Insert code here to initialize your application
        /*
         Commented-out as for now. Probably we should set
         some custom view (statusItem.button) instead and
         show PM 2.5, PM 10 in status bar directly.
        statusItem.title = ""
        statusItem.menu = statusBarMenu
        let img = NSImage.init(named: NSImage.Name.applicationIcon)
        img?.size = NSSize(width: 15, height: 15)
        statusItem.image = img
 */
    }

    func applicationWillTerminate(_ aNotification: Notification) {
        // Insert code here to tear down your application
    }

    func applicationShouldTerminateAfterLastWindowClosed(_ sender: NSApplication) -> Bool {
        return true
    }

}

class Window : NSWindow { }

