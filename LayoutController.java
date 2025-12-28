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
        
        System.out.println("=================================================");
        System.out.println("🔧 LayoutController INITIALIZE START");
        System.out.println("=================================================");
        
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ No user session!");
            AlertUtil.showError("Error", "Session tidak valid!");
            handleLogout();
            return;
        }

        welcomeLabel.setText("Halo, " + currentUser.getNama());
        roleLabel.setText(getRoleDisplayName(currentUser.getRole()));
        
        System.out.println("👤 Current User: " + currentUser.getNama());
        System.out.println("🎭 Current Role: " + currentUser.getRole());
        
        // DEBUG: Check if buttons are loaded from FXML
        System.out.println("\n📋 Button Loading Status:");
        System.out.println("  btnDashboard: " + (btnDashboard != null ? "✅ Loaded" : "❌ NULL"));
        System.out.println("  btnPeminjaman: " + (btnPeminjaman != null ? "✅ Loaded" : "❌ NULL"));
        System.out.println("  btnPengembalian: " + (btnPengembalian != null ? "✅ Loaded" : "❌ NULL"));
        System.out.println("  btnLaporan: " + (btnLaporan != null ? "✅ Loaded" : "❌ NULL"));
        System.out.println("  btnBarang: " + (btnBarang != null ? "✅ Loaded" : "❌ NULL"));
        System.out.println("  btnUser: " + (btnUser != null ? "✅ Loaded" : "❌ NULL"));
        System.out.println("  btnLapor: " + (btnLapor != null ? "✅ Loaded" : "❌ NULL"));
        System.out.println("  btnBerita: " + (btnBerita != null ? "✅ Loaded" : "❌ NULL"));
        System.out.println("  btnVerifikasiReturn: " + (btnVerifikasiReturn != null ? "✅ Loaded" : "❌ NULL"));

        System.out.println("\n🔧 Configuring menu for role: " + currentUser.getRole());
        configureMenuByRole(currentUser.getRole());
        
        handleHome();
        
        System.out.println("=================================================");
        System.out.println("✅ LayoutController INITIALIZE COMPLETE");
        System.out.println("=================================================\n");
    }

    private void configureMenuByRole(String role) {
        System.out.println("\n🎯 === CONFIGURE MENU BY ROLE ===");
        System.out.println("Role received: '" + role + "'");
        
        // Step 1: Hide ALL buttons first
        System.out.println("\n📴 Step 1: Hiding all buttons...");
        hideAllButtons();
        
        // Step 2: Show buttons based on role
        System.out.println("\n📱 Step 2: Showing buttons for role '" + role + "'...");
        
        if ("admin".equals(role)) {
            System.out.println("🔑 ADMIN MODE ACTIVATED");
            
            if (btnBerita != null) {
                btnBerita.setVisible(true);
                btnBerita.setManaged(true);
                System.out.println("  ✅ btnBerita: VISIBLE");
            } else {
                System.out.println("  ❌ btnBerita: NULL!");
            }
            
            if (btnUser != null) {
                btnUser.setVisible(true);
                btnUser.setManaged(true);
                System.out.println("  ✅ btnUser: VISIBLE");
            } else {
                System.out.println("  ❌ btnUser: NULL!");
            }
            
            if (btnLapor != null) {
                btnLapor.setVisible(true);
                btnLapor.setManaged(true);
                System.out.println("  ✅ btnLapor: VISIBLE");
            } else {
                System.out.println("  ❌ btnLapor: NULL!");
            }
            
            if (btnVerifikasiReturn != null) {
                btnVerifikasiReturn.setVisible(true);
                btnVerifikasiReturn.setManaged(true);
                System.out.println("  ✅ btnVerifikasiReturn: VISIBLE");
                System.out.println("      Text: " + btnVerifikasiReturn.getText());
                System.out.println("      Managed: " + btnVerifikasiReturn.isManaged());
                System.out.println("      Visible: " + btnVerifikasiReturn.isVisible());
            } else {
                System.out.println("  ❌❌❌ btnVerifikasiReturn: NULL! ❌❌❌");
                System.out.println("  ⚠️ MASALAH: Button tidak ter-load dari FXML!");
            }
            
        } else if ("peminjam".equals(role)) {
            System.out.println("👤 PEMINJAM MODE ACTIVATED");
            showButton(btnBarang, "btnBarang");
            showButton(btnPeminjaman, "btnPeminjaman");
            showButton(btnLaporan, "btnLaporan");
            showButton(btnPengembalian, "btnPengembalian");
            
        } else if ("instansi".equals(role)) {
            System.out.println("🏢 INSTANSI MODE ACTIVATED");
            showButton(btnBarang, "btnBarang");
            showButton(btnPeminjaman, "btnPeminjaman");
        } else {
            System.err.println("⚠️ UNKNOWN ROLE: " + role);
        }
        
        System.out.println("\n=== END CONFIGURE MENU ===\n");
    }
    
    private void hideAllButtons() {
        hideButton(btnBarang);
        hideButton(btnPeminjaman);
        hideButton(btnLaporan);
        hideButton(btnUser);
        hideButton(btnLapor);
        hideButton(btnBerita);
        hideButton(btnPengembalian);
        hideButton(btnVerifikasiReturn);
        System.out.println("  All buttons hidden");
    }
    
    private void showButton(Button btn, String name) {
        if (btn != null) {
            btn.setVisible(true);
            btn.setManaged(true);
            System.out.println("  ✅ " + name + ": VISIBLE");
        } else {
            System.out.println("  ❌ " + name + ": NULL!");
        }
    }
    
    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }

    @FXML
    public void handleHome() {
        System.out.println("📂 handleHome() → Loading Home.fxml");
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
    public void handlePengembalian() {
        System.out.println("📂 handlePengembalian() → Peminjam ajukan pengembalian");
        setActiveMenu(btnPengembalian);
        loadPage("/fxml/PengembalianPeminjam.fxml");
    }

    @FXML
    private void handleVerifikasiReturn() {
        System.out.println("📂 handleVerifikasiReturn() → Admin verifikasi pengembalian");
        setActiveMenu(btnVerifikasiReturn);
        loadPage("/fxml/PengembalianAdmin.fxml");
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

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin": return "Administrator";
            case "peminjam": return "Peminjam";
            case "instansi": return "Instansi";
            default: return role;
        }
    }
}
