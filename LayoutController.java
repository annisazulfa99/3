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
 * UPDATED: Tambah fitur Pengembalian untuk Peminjam & Admin
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
    
    // ✅ BUTTON BARU: Pengembalian
    @FXML private Button btnPengembalian;
    @FXML private Button btnVerifikasiReturn;
    
    private String lastLoadedFxml = "/fxml/Home.fxml";
    
    private final String DEFAULT_STYLE = "-fx-background-color: #D9CBC1; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;";
    private final String ACTIVE_STYLE = "-fx-background-color: #8C6E63; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 16px;";
    
    @FXML private TextField txtSearch;
    @FXML public StackPane contentArea;
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
        handleHome();
        
        System.out.println("✅ LayoutController initialized");
    }

    // =================================================================
    // ✅ NAVIGASI BARU: PENGEMBALIAN
    // =================================================================

    /**
     * Untuk PEMINJAM - Ajukan Pengembalian
     */
    @FXML
    public void handlePengembalian() {
        System.out.println("📂 handlePengembalian() → Peminjam ajukan pengembalian");
        setActiveMenu(btnPengembalian);
        loadPage("/fxml/PengembalianPeminjam.fxml");
    }

    /**
     * Untuk ADMIN - Verifikasi Pengembalian
     */
    @FXML
    private void handleVerifikasiReturn() {
        System.out.println("📂 handleVerifikasiReturn() → Admin verifikasi pengembalian");
        setActiveMenu(btnVerifikasiReturn);
        loadPage("/fxml/PengembalianAdmin.fxml");
    }

    // =================================================================
    // NAVIGASI MENU LAINNYA (EXISTING)
    // =================================================================

    @FXML
    public void handleHome() {
        System.out.println("📂 handleHome() (SIMAK clicked) → Loading Home.fxml");
        setActiveMenu(null);
        loadPage("/fxml/Home.fxml");
    }

    @FXML
    private void handleDashboard() {
        System.out.println("📂 handleDashboard() → Loading Dashboard.fxml");
        setActiveMenu(btnDashboard);
        loadPage("/fxml/Dashboard.fxml");
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

        String role = sessionManager.getCurrentRole();

        if ("peminjam".equals(role)) {
            loadPage("/fxml/DataBarang.fxml");
        } else if ("instansi".equals(role)) {
            loadPage("/fxml/BarangInstansi.fxml");
        } 
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
        if (btnPengembalian != null) btnPengembalian.setStyle(DEFAULT_STYLE);
        if (btnVerifikasiReturn != null) btnVerifikasiReturn.setStyle(DEFAULT_STYLE);
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
        // Reset semua tombol dulu
        if (btnBarang != null) { btnBarang.setVisible(false); btnBarang.setManaged(false); }
        if (btnPeminjaman != null) { btnPeminjaman.setVisible(false); btnPeminjaman.setManaged(false); }
        if (btnLaporan != null) { btnLaporan.setVisible(false); btnLaporan.setManaged(false); }
        if (btnUser != null) { btnUser.setVisible(false); btnUser.setManaged(false); }
        if (btnLapor != null) { btnLapor.setVisible(false); btnLapor.setManaged(false); }
        if (btnBerita != null) { btnBerita.setVisible(false); btnBerita.setManaged(false); }
        if (btnPengembalian != null) { btnPengembalian.setVisible(false); btnPengembalian.setManaged(false); }
        if (btnVerifikasiReturn != null) { btnVerifikasiReturn.setVisible(false); btnVerifikasiReturn.setManaged(false); }

        switch (role) {
            case "admin":
                // Admin: Full access + Verifikasi Return
                if (btnBerita != null) { btnBerita.setVisible(true); btnBerita.setManaged(true); }
                if (btnUser != null) { btnUser.setVisible(true); btnUser.setManaged(true); }
                if (btnLapor != null) { btnLapor.setVisible(true); btnLapor.setManaged(true); }
                if (btnVerifikasiReturn != null) { btnVerifikasiReturn.setVisible(true); btnVerifikasiReturn.setManaged(true); }
                break;

            case "peminjam":
                // Peminjam: Katalog, Peminjaman, Lapor, Pengembalian
                if (btnBarang != null) { btnBarang.setVisible(true); btnBarang.setManaged(true); }
                if (btnPeminjaman != null) { btnPeminjaman.setVisible(true); btnPeminjaman.setManaged(true); }
                if (btnLaporan != null) { btnLaporan.setVisible(true); btnLaporan.setManaged(true); }
                if (btnPengembalian != null) { btnPengembalian.setVisible(true); btnPengembalian.setManaged(true); }
                break;

            case "instansi":
                // Instansi: CRUD barang, verifikasi peminjaman (TIDAK ada pengembalian)
                if (btnBarang != null) { btnBarang.setVisible(true); btnBarang.setManaged(true); }
                if (btnPeminjaman != null) { btnPeminjaman.setVisible(true); btnPeminjaman.setManaged(true); }
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

