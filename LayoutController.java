package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.model.User;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * LayoutController - Main Layout Controller
 * 
 * PENTING - MAPPING YANG BENAR:
 * - Button "Dashboard" → handleDashboard() → load Home.fxml (DashboardController) → BERITA + REKOMENDASI
 * - Button "SIMAK" → handleHome() → load Home.fxml (DashboardController) → BERITA + REKOMENDASI
 * - Menu lain (jika ada) → handleStatistik() → load Dashboard.fxml (HomeController) → STATISTIK
 */
public class LayoutController implements Initializable {

    @FXML private Circle profileImage;
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnLapor;
    @FXML private Button btnPeminjaman;
    @FXML private Button btnLaporan;
    @FXML private Button btnBarang;
    @FXML private Button btnUser;
    @FXML private Button btnBerita;
    
    // ✅ DEFAULT: Home.fxml (Berita + Rekomendasi)
    private String lastLoadedFxml = "/fxml/Home.fxml";
    
    private final String DEFAULT_STYLE = "-fx-background-color: #D9CBC1; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;";
    private final String ACTIVE_STYLE = "-fx-background-color: #8C6E63; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 16px;";
    
    @FXML private TextField txtSearch;
    @FXML public StackPane contentArea; // PUBLIC untuk DashboardController
    private static LayoutController instance;
    
    private final SessionManager sessionManager = SessionManager.getInstance();
    private Parent currentContent;

    public static LayoutController getInstance() {
        return instance;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        
        System.out.println("🔧 LayoutController.initialize() started");
        
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ No user session!");
            AlertUtil.showError("Error", "Session tidak valid!");
            handleLogout();
            return;
        }

        welcomeLabel.setText("Halo, " + currentUser.getNama());
        roleLabel.setText(getRoleDisplayName(currentUser.getRole()));
        
        System.out.println("👤 User: " + currentUser.getNama() + " (" + currentUser.getRole() + ")");

        configureMenuByRole(currentUser.getRole());

        // ✅ CRITICAL: Load Home.fxml sebagai halaman DEFAULT (awal login)
        System.out.println("🎯 Loading DEFAULT page: Home.fxml (Berita + Rekomendasi)");
        handleHome(); // ✅ Load Home.fxml dulu sebagai halaman awal
        
        System.out.println("✅ LayoutController initialized");
    }

    // =================================================================
    // NAVIGASI MENU - YANG BENAR
    // =================================================================

    /**
     * ✅ Label "SIMAK" diklik → Load HOME (Berita + Rekomendasi)
     * File: Home.fxml
     * Controller: DashboardController
     */
    @FXML
    public void handleHome() { // ✅ PUBLIC untuk bisa dipanggil dari FXML
        System.out.println("📂 handleHome() (SIMAK clicked) → Loading Home.fxml (BERITA + REKOMENDASI)");
        
        try {
            setActiveMenu(null); // Reset menu, karena SIMAK bukan button menu
            loadPage("/fxml/Home.fxml"); // ✅ Home.fxml = DashboardController = BERITA
        } catch (Exception e) {
            System.err.println("❌ Error in handleHome(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Button "Dashboard" → Load STATISTIK DASHBOARD
     * File: Dashboard.fxml
     * Controller: HomeController
     */
    @FXML
    private void handleDashboard() {
        System.out.println("📂 handleDashboard() → Loading Dashboard.fxml (STATISTIK)");
        setActiveMenu(btnDashboard);
        loadPage("/fxml/Dashboard.fxml"); // ✅ Dashboard.fxml = HomeController = STATISTIK
    }

    /**
     * ⚠️ DEPRECATED - Untuk backward compatibility
     * Gunakan handleDashboard() untuk Statistik
     */
    @FXML
    private void handleStatistik() {
        System.out.println("📂 handleStatistik() → Redirecting to handleDashboard()");
        handleDashboard();
    }

    @FXML
    public void handlePeminjaman() {
        System.out.println("📂 handlePeminjaman()");
        setActiveMenu(btnPeminjaman);
        loadPage("/fxml/Peminjaman.fxml");
    }

    @FXML
    private void handleLaporan() {
        System.out.println("📂 handleLaporan()");
        setActiveMenu(btnLaporan);
        loadPage("/fxml/LaporanPeminjam.fxml");
    }
    
    @FXML
    private void handleLapor() {
        System.out.println("📂 handleLapor()");
        setActiveMenu(btnLapor);
        loadPage("/fxml/LaporanAdmin.fxml");
    }

    @FXML
    public void handleBarang() {
        System.out.println("📂 handleBarang()");
        setActiveMenu(btnBarang);
        loadPage("/fxml/DataBarang.fxml");
    }

    @FXML
    private void handleUser() {
        System.out.println("📂 handleUser()");
        setActiveMenu(btnUser);
        loadPage("/fxml/User.fxml");
    }

    @FXML
    private void handleBerita() {
        System.out.println("📂 handleBerita()");
        setActiveMenu(btnBerita);
        loadPage("/fxml/Berita-view.fxml");
    }
    
    // =================================================================
    // HELPER METHODS
    // =================================================================

    public void updateContentArea(Parent newContent) {
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
            System.out.println("✅ Content area updated");
        }
    }

    public void setActiveBarangMenu() {
        setActiveMenu(btnBarang);
    }

    // =================================================================
    // SEARCH
    // =================================================================

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) return;

        System.out.println("🔍 Searching: " + keyword);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DataBarang.fxml"));
            Parent page = loader.load();
            
            Object controller = loader.getController();
            
            if (controller instanceof DataBarangPeminjamController) {
                ((DataBarangPeminjamController) controller).searchBarang(keyword);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
            setActiveMenu(btnBarang);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Gagal melakukan pencarian.");
        }
    }

    // =================================================================
    // STYLING
    // =================================================================

    private void resetAllMenus() {
        if (btnDashboard != null) btnDashboard.setStyle(DEFAULT_STYLE);
        if (btnPeminjaman != null) btnPeminjaman.setStyle(DEFAULT_STYLE);
        if (btnLaporan != null) btnLaporan.setStyle(DEFAULT_STYLE);
        if (btnBarang != null) btnBarang.setStyle(DEFAULT_STYLE);
        if (btnUser != null) btnUser.setStyle(DEFAULT_STYLE);
        if (btnBerita != null) btnBerita.setStyle(DEFAULT_STYLE);
        if (btnLapor != null) btnLapor.setStyle(DEFAULT_STYLE);
    }

    private void setActiveMenu(Button activeButton) {
        resetAllMenus();
        if (activeButton != null) {
            activeButton.setStyle(ACTIVE_STYLE);
        }
    }

    // =================================================================
    // UTILITIES
    // =================================================================

    @FXML
    private void handleLogout() {
        if (AlertUtil.showLogoutConfirmation()) {
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null) {
                LogActivityUtil.logLogout(currentUser.getUsername(), currentUser.getRole());
            }
            
            sessionManager.logout();
            
            try {
                Main.showLoginScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Refreshing: " + lastLoadedFxml);
        loadPage(lastLoadedFxml);
        
        if (txtSearch != null) {
            txtSearch.clear();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            this.lastLoadedFxml = fxmlPath;

            System.out.println("📂 Loading: " + fxmlPath);
            
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            currentContent = loader.load();
            contentArea.getChildren().add(currentContent);
            
            System.out.println("✅ Loaded successfully: " + fxmlPath);
            
        } catch (IOException e) {
            System.err.println("❌ Failed to load: " + fxmlPath);
            e.printStackTrace();
            
            AlertUtil.showError("Error", 
                "Gagal memuat halaman!\n" +
                "File: " + fxmlPath + "\n" +
                "Error: " + e.getMessage()
            );
        }
    }

    private void configureMenuByRole(String role) {
        if (btnBarang != null) { btnBarang.setVisible(false); btnBarang.setManaged(false); }
        if (btnPeminjaman != null) { btnPeminjaman.setVisible(false); btnPeminjaman.setManaged(false); }
        if (btnLaporan != null) { btnLaporan.setVisible(false); btnLaporan.setManaged(false); }
        if (btnUser != null) { btnUser.setVisible(false); btnUser.setManaged(false); }
        if (btnLapor != null) { btnLapor.setVisible(false); btnLapor.setManaged(false); }
        if (btnBerita != null) { btnBerita.setVisible(false); btnBerita.setManaged(false); }

        switch (role) {
            case "admin":
                if (btnBerita != null) { btnBerita.setVisible(true); btnBerita.setManaged(true); }
                if (btnUser != null) { btnUser.setVisible(true); btnUser.setManaged(true); }
                if (btnLapor != null) { btnLapor.setVisible(true); btnLapor.setManaged(true); }
                break;
                
            case "peminjam":
                if (btnBarang != null) { btnBarang.setVisible(true); btnBarang.setManaged(true); }
                if (btnPeminjaman != null) { btnPeminjaman.setVisible(true); btnPeminjaman.setManaged(true); }
                if (btnLaporan != null) { btnLaporan.setVisible(true); btnLaporan.setManaged(true); }
                break;
                
            case "instansi":
                if (btnBarang != null) { btnBarang.setVisible(true); btnBarang.setManaged(true); }
                if (btnPeminjaman != null) { btnPeminjaman.setVisible(true); btnPeminjaman.setManaged(true); }
                if (btnLapor != null) { btnLapor.setVisible(true); btnLapor.setManaged(true); }
                break;
        }
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin": return "Administrator";
            case "peminjam": return "Peminjam";
            case "instansi": return "Instansi";
            default: return role;
        }
    }
}