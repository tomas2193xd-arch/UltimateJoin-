package dev.tomasgarcia.ultimatejoinplus.utils;

import dev.tomasgarcia.ultimatejoinplus.UltimateJoinPlus;
import dev.tomasgarcia.ultimatejoinplus.utils.ColorUtils;

import java.util.logging.Level;

public class SmartErrorHandler {

        public static void handleConfigError(String context, Exception e) {
                // Check if it's a likely user configuration error (enum not found, mostly)
                String msg = e.getMessage();

                // Console Alert for Config Errors
                UltimateJoinPlus.getInstance().getLogger()
                                .warning(ColorUtils.ANSI_YELLOW + "⚠ CONFIGURATION ERROR DETECTED ⚠"
                                                + ColorUtils.ANSI_RESET);
                UltimateJoinPlus.getInstance().getLogger().warning(
                                ColorUtils.ANSI_YELLOW + "Location: " + ColorUtils.ANSI_WHITE + context
                                                + ColorUtils.ANSI_RESET);

                if (e instanceof IllegalArgumentException || e instanceof NullPointerException) {
                        UltimateJoinPlus.getInstance().getLogger().warning(ColorUtils.ANSI_YELLOW + "Issue: "
                                        + ColorUtils.ANSI_WHITE + "Invalid value or missing key."
                                        + ColorUtils.ANSI_RESET);
                        UltimateJoinPlus.getInstance().getLogger()
                                        .warning(ColorUtils.ANSI_YELLOW + "Suggestion: " + ColorUtils.ANSI_WHITE
                                                        + "Check for typos in sounds, particles, or material names."
                                                        + ColorUtils.ANSI_RESET);
                        UltimateJoinPlus.getInstance().getLogger().warning(
                                        ColorUtils.ANSI_YELLOW + "Details: " + ColorUtils.ANSI_WHITE + msg
                                                        + ColorUtils.ANSI_RESET);
                } else {
                        // Unexpected error, print full stack trace
                        UltimateJoinPlus.getInstance().getLogger().log(Level.SEVERE, "Unexpected error in " + context,
                                        e);
                }

                UltimateJoinPlus.getInstance().getLogger()
                                .warning(ColorUtils.ANSI_YELLOW + "--------------------------------"
                                                + ColorUtils.ANSI_RESET);
        }
}
