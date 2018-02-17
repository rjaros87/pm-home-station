//
//  InfoViewController.swift
//  PMMonitor
//
//  Created by Jerzy Łukjaniec on 15.02.2018.
//  License: GPL 3.0
//

import UIKit
import WebKit

class InfoViewController: UIViewController, WKNavigationDelegate {

    @IBOutlet var htmlView: WKWebView!

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        if let (html, baseURL) = loadData() {
            htmlView.loadHTMLString(html, baseURL: baseURL)
        } else {
            // This should never happen ;)
            // as html file is stored in app bundle
            let html = "<html><body><h1>Internal inconsistensy</h1></body></html>"
            htmlView.loadHTMLString(html, baseURL: nil)
        }

        htmlView.navigationDelegate = self
    }

    @IBAction func dismiss() {
        self.dismiss(animated: true, completion: nil)
    }

    private func loadData() -> (String, URL)? {
        let bundle = Bundle.main
        guard let url = bundle.url(forResource: "About", withExtension: "html") else {
            return nil
        }

        do {
            let html = try String(contentsOf: url, encoding: .utf8)
            return (html, url.deletingLastPathComponent())
        } catch {
            return nil
        }
    }

    // We will open all urls in Safari, as we don't want to provide possibility to browse internet inside application :)
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {

        if navigationAction.navigationType == .linkActivated {
            let app = UIApplication.shared
            if let url = navigationAction.request.url, app.canOpenURL(url) {
                app.open(url, options: [:], completionHandler: nil)
                decisionHandler(.cancel)
                return
            }
        }

        decisionHandler(.allow)
    }

    

}
