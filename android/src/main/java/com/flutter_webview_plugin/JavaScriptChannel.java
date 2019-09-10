// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.flutter_webview_plugin;

import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;

import java.util.HashMap;

import io.flutter.plugin.common.MethodChannel;

/**
 * Added as a JavaScript interface to the WebView for any JavaScript channel that the Dart code sets
 * up.
 *
 * <p>Exposes a single method named `postMessage` to JavaScript, which sends a message over a method
 * channel to the Dart code.
 */
class JavaScriptChannel {
    private final MethodChannel methodChannel;
    private final String javaScriptChannelName;
    private final Handler platformThreadHandler;
    private final String key;

    /**
     * @param methodChannel         the Flutter WebView method channel to which JS messages are sent
     * @param javaScriptChannelName the name of the JavaScript channel, this is sent over the method
     *                              channel with each message to let the Dart code know which JavaScript channel the message
     *                              was sent through
     */
    JavaScriptChannel(
            MethodChannel methodChannel, String javaScriptChannelName, String key, Handler platformThreadHandler) {
        this.methodChannel = methodChannel;
        this.javaScriptChannelName = javaScriptChannelName;
        this.platformThreadHandler = platformThreadHandler;
        this.key = key;
    }

    // Suppressing unused warning as this is invoked from JavaScript.
    @SuppressWarnings("unused")
    @JavascriptInterface
    public void postMessage(final String message) {
        Runnable postMessageRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, String> arguments = new HashMap<>();
                        arguments.put("channel", javaScriptChannelName);
                        arguments.put("message", message);
                        arguments.put("keyWebView", key);
                        methodChannel.invokeMethod("javascriptChannelMessage", arguments);
                    }
                };
        if (platformThreadHandler.getLooper() == Looper.myLooper()) {
            postMessageRunnable.run();
        } else {
            platformThreadHandler.post(postMessageRunnable);
        }
    }
}
