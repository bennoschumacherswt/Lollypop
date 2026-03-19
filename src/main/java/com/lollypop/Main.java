package com.lollypop;

/**
 * Legacy CLI entry point — kept for reference.
 * The application now starts via {@link LollypopApp} (JavaFX).
 *
 * To run the CLI version use the original Main class logic;
 * the JavaFX UI is the primary interface.
 */
public class Main {
    public static void main(String[] args) {
        // Delegate to JavaFX launcher
        LollypopApp.main(args);
    }
}
