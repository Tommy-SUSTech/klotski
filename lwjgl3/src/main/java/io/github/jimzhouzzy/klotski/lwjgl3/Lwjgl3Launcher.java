package io.github.jimzhouzzy.klotski.lwjgl3;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import io.github.jimzhouzzy.klotski.ConfigPathHelper;
import io.github.jimzhouzzy.klotski.Klotski;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        if (StartupHelper.startNewJvmIfRequired())
            return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        Klotski klotski = new Klotski();
        Lwjgl3ApplicationConfiguration config = getDefaultConfiguration();
        klotski.setLwjgl3Config(config);
        Lwjgl3Application app = new Lwjgl3Application(klotski, config);
        return app;
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        boolean[] rawConfiguration = loadConfiguration();

        boolean antialiasingEnabled = rawConfiguration[0];
        boolean useVsync = rawConfiguration[1];
        int samples = antialiasingEnabled ? 4 : 0;

        configuration.setTitle("Klotski");
        //// Vsync limits the frames per second to what your hardware can display, and
        //// helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line
        //// after is a safeguard.
        configuration.useVsync(useVsync);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to
        //// try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match
        //// the monitor.
        // JimZhouZZY: Maybe we should set this to 60 FPS for all platforms?
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited
        //// FPS, which can be
        //// useful for testing performance, but can also be very stressful to some
        //// hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can
        //// cause screen tearing.

        configuration.setBackBufferConfig(
                8, 8, 8, 8, // r, g, b, a bits 2^8
                16, // depth buffer
                8, // stencil buffer
                samples // samples for MSAA
        );

        configuration.setWindowedMode(1080, 760);
        configuration.setResizable(true);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }

    private static boolean[] loadConfiguration() {
        // TODO: Add frame rate option

        final ConfigPathHelper configPathHelper = new ConfigPathHelper();
        final String SETTINGS_FILE = configPathHelper.getConfigFilePath("Klotski", "settings.json");
        File file = new File(SETTINGS_FILE);
        Map<String, Object> settings;

        if (!file.exists()) {
            settings = getDefaultSettings();
        } else {
            try (FileReader reader = new FileReader(file)) {
                Json json = new Json();
                settings = json.fromJson(HashMap.class, reader);

                // Validate the settings file
                if (!isSettingsValid(settings)) {
                    System.out.println("Settings file is invalid. Using default settings.");
                    settings = getDefaultSettings();
                }
            } catch (Exception e) {
                System.out.println("Failed to load settings file. Using default settings. Error: " + e.getMessage());
                settings = getDefaultSettings();
            }
        }

        // Do not specifically log username for now
        String username = "default";
        boolean antialiasingEnabled = (boolean) settings.get("antialiasingEnabled");
        boolean useVsync = (boolean) settings.get("vsyncEnabled");
        boolean musicEnabled = (boolean) settings.get("musicEnabled");
        return new boolean[] { antialiasingEnabled, useVsync, musicEnabled };
    }

    private static boolean isSettingsValid(Map<String, Object> settings) {
        try {
            // Check if all required keys are present and of the correct type
            if (!settings.containsKey("isDarkMode") || !(settings.get("isDarkMode") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("antialiasingEnabled")
                    || !(settings.get("antialiasingEnabled") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("vsyncEnabled") || !(settings.get("vsyncEnabled") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("musicEnabled") || !(settings.get("vsyncEnabled") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("username") || !(settings.get("username") instanceof String)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Gdx.app.log("Settings", "Error validating settings: " + e.getMessage());
            return false;
        }
    }

    private static Map<String, Object> getDefaultSettings() {
        Map<String, Object> defaultSettings = new HashMap<>();
        defaultSettings.put("isDarkMode", false); // Default to light mode
        defaultSettings.put("username", "Guest"); // Default username
        defaultSettings.put("antialiasingEnabled", true);
        defaultSettings.put("vsyncEnabled", true);
        defaultSettings.put("musicEnabled", true);
        return defaultSettings;
    }
}
