package com.inventaris.controller;

import com.inventaris.model.User;
import com.inventaris.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * HomeController - Controller untuk Dashboard.fxml
 * Menampilkan STATISTIK DASHBOARD (sementara kosong, akan dikembangkan nanti)
 * 
 * CATATAN: Nama file dan controller memang TERTUKAR, tapi jangan diubah!
 * - File FXML: Dashboard.fxml
 * - Controller: HomeController.java
 * - Fungsi: Menampilkan statistik peminjaman (placeholder untuk sekarang)
 */
public class HomeController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label infoLabel;
    
    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 HomeController (Dashboard.fxml) initializing...");
        
        User currentUser = sessionManager.getCurrentUser();
        
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Selamat Datang, " + currentUser.getNama());
        }
        
        if (infoLabel != null) {
            infoLabel.setText("Halaman Statistik Dashboard akan segera hadir!");
        }
        
        System.out.println("✅ HomeController initialized (Statistik - Placeholder)");
    }
    
    /**
     * Method untuk load statistik (akan dikembangkan nanti)
     * Untuk sekarang dibiarkan kosong dulu
     */
    private void loadStatistics() {
        // TODO: Implementasi load statistik peminjaman
        // - Total barang
        // - Barang tersedia
        // - Barang dipinjam
        // - Barang terlambat
        // - Chart/grafik peminjaman
        System.out.println("📊 Statistics loading (placeholder)...");
    }
}