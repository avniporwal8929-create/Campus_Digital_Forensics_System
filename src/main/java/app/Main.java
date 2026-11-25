package app;

import config.AppConfig;
import config.DBConnection;
import ui.LoginFrame;
import ui.Theme;

import javax.swing.*;

/**
 * Campus Digital Forensics System (CDF) - Main Entry Point
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("CAMPUS DIGITAL FORENSICS SYSTEM (CDF) - INITIALIZING");
        System.out.println("Runtime: Java " + System.getProperty("java.version"));
        System.out.println("Target Database: MySQL 8.0 (" + AppConfig.getHost() + ":" + AppConfig.getPort() + "/" + AppConfig.getDatabase() + ")");
        System.out.println("==========================================================");

        // Apply dark navy & electric blue UI theme
        Theme.applyGlobalTheme();

        // Check initial database connectivity
        boolean dbLive = DBConnection.testConnection();
        if (dbLive) {
            System.out.println("✔ MySQL Relational Database: CONNECTED (Live Mode)");
        } else {
            System.out.println("▲ MySQL Database Status: OFFLINE / ACCESS ISSUE");
            System.out.println("  Detail: " + DBConnection.getLastErrorMessage());
            System.out.println("  [NOTICE] The application will launch with full relational simulated datasets.");
            System.out.println("  [NOTICE] You can connect to live MySQL anytime via the '⚙ Database' button.");
        }

        // Launch Desktop Swing Application Window
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
