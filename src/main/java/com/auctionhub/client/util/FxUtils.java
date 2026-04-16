package com.auctionhub.client.util;

import javafx.application.Platform;

public final class FxUtils {
    private FxUtils() {
    }

    public static void runOnUiThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}
