package net.chris.lib.network.retrofit;

import okhttp3.internal.platform.Platform;

public interface NetworkLogger {

    void log(String message);

    /**
     * A {@link NetworkLogger} defaults output appropriate for the current platform.
     */
    NetworkLogger DEFAULT = new NetworkLogger() {
        @Override
        public void log(String message) {
            Platform.get().log(Platform.INFO, message, null);
        }
    };
}
