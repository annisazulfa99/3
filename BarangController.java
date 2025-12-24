// ================================================================
// File: src/main/java/com/inventaris/controller/BarangController.java
// FIXED VERSION - All errors resolved
// ================================================================
package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.dao.BarangDAO;
import com.inventaris.model.Barang;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import com.inventaris.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;

/**
 * BarangController - Manage CRUD operations for Barang
 * UPDATED: Added instansi ownership & role-based authorization
 */
public class BarangController implements Initializable {
    
    // ============================================================
    // FXML FIELDS
    // ============================================================
    @FXML private Button btnDashboard;
    @FXML private Button btnBarang;
    @FXML private Button btnPeminjaman;
    @FXML private Button btnLaporan;
    @FXML private Button btnLogout;
    @FXML private Button btnUser;
    // Form Fields
    @FXML private TextField kodeBarangField;
    @FXML private TextField namaBarangField;
    @FXML private TextField lokasiField;
    @FXML private TextField jumlahTotalField;
    @FXML private TextField jumlahTersediaField;
    @FXML private TextArea deskripsiArea;
    @FXML private ComboBox<String> kondisiCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> instansiCombo; // NEW: Instansi ownership
    
    // Table
    @FXML private TableView<Barang> barangTable;
    @FXML private TableColumn<Barang, String> colKode;
    @FXML private TableColumn<Barang, String> colNama;
    @FXML private TableColumn<Barang, String> colLokasi;
    @FXML private TableColumn<Barang, Integer> colTotal;
    @FXML private TableColumn<Barang, Integer> colTersedia;
    @FXML private TableColumn<Barang, String> colKondisi;
    @FXML private TableColumn<Barang, String> colStatus;
    @FXML private TableColumn<Barang, String> colPemilik; // NEW: Owner column
    
    // Buttons & Search
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private TextField searchField;
    
    // ============================================================
    // FIELDS
    // ============================================================
    
    private final BarangDAO barangDAO = new BarangDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private Barang selectedBarang;
    private boolean isEditMode = false;
    
    private void configureMenuByRole(String role) {
    switch (role) {
        case "admin":
            btnBarang.setVisible(true);
            btnPeminjaman.setVisible(true);
            btnLaporan.setVisible(true);
            btnUser.setVisible(true);
            btnUser.setManaged(true); // ✅ TAMBAH BARIS INI
            break;
            
        case "peminjam":
            btnBarang.setVisible(true);
            btnPeminjaman.setVisible(true);
            btnLaporan.setVisible(true);
            btnUser.setVisible(false);
            btnUser.setManaged(false);
            break;
            
        case "instansi":
            btnBarang.setVisible(true);
            btnPeminjaman.setVisible(true);
            btnLaporan.setVisible(true);
            btnUser.setVisible(false);
            btnUser.setManaged(false); // ✅ TAMBAH BARIS INI
            break;
            
        default:
            btnBarang.setVisible(false);
            btnPeminjaman.setVisible(false);
            btnLaporan.setVisible(false);
            btnUser.setVisible(false);
            btnUser.setManaged(false); // ✅ TAMBAH BARIS INI
    }
}
    // ============================================================
    // INITIALIZATION
    // ============================================================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ComboBoxes
        kondisiCombo.setItems(FXCollections.observableArrayList(
            "baik", "rusak ringan", "rusak berat"
        ));
        kondisiCombo.setValue("baik");
        configureMenuByRole(sessionManager.getCurrentRole());
        statusCombo.setItems(FXCollections.observableArrayList(
            "tersedia", "dipinjam", "rusak", "hilang"
        ));
        statusCombo.setValue("tersedia");
        
        // Configure table columns
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colLokasi.setCellValueFactory(new PropertyValueFactory<>("lokasiBarang"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("jumlahTotal"));
        colTersedia.setCellValueFactory(new PropertyValueFactory<>("jumlahTersedia"));
        colKondisi.setCellValueFactory(new PropertyValueFactory<>("kondisiBarang"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Setup column pemilik (NEW)
        colPemilik.setCellValueFactory(cellData -> {
            String pemilik = cellData.getValue().getNamaPemilik();
            return new javafx.beans.property.SimpleStringProperty(
                pemilik != null ? pemilik : "Umum"
            );
        });
        
        // Table selection listener
        barangTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedBarang = newSelection;
                    populateForm(newSelection);
                    setEditMode(true);
                }
            }
        );
        
        // Load instansi list (NEW)
        loadInstansiList();
        
        // Role-based initialization
        if (sessionManager.isPeminjam()) {
            // Peminjam: read-only mode
            disableEditingForPeminjam();
            loadBarangData(); // Show all barang
            
        } else if (sessionManager.isInstansi()) {
            // Instansi: only their barang
            loadBarangDataForInstansi();
            
        } else if (sessionManager.isAdmin()) {
            // Admin: full access
            loadBarangData(); // Show all barang
        }
        
        // Set initial button states
        setEditMode(false);
        
        System.out.println("✅ Barang Controller initialized for role: " + sessionManager.getCurrentRole());
    }
    
    // ============================================================
    // DATA LOADING
    // ============================================================
    
    /**
     * Load all barang data (role-based)
     */
    private void loadBarangData() {
        try {
            List<Barang> barangList;
            
            if (sessionManager.isAdmin() || sessionManager.isPeminjam()) {
                // Admin & Peminjam: lihat SEMUA
                barangList = barangDAO.getAll();
            } else if (sessionManager.isInstansi()) {
                // Instansi: hanya barang sendiri
                Integer instansiId = sessionManager.getCurrentRoleId();
                barangList = barangDAO.getByInstansi(instansiId);
            } else {
                barangList = new ArrayList<>();
            }
            
            ObservableList<Barang> observableList = FXCollections.observableArrayList(barangList);
            barangTable.setItems(observableList);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data barang!");
            e.printStackTrace();
        }
    }
    
    /**
     * Load barang for instansi (only their items)
     */
    private void loadBarangDataForInstansi() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            if (instansiId == null) return;
            
            List<Barang> barangList = barangDAO.getByInstansi(instansiId);
            ObservableList<Barang> observableList = FXCollections.observableArrayList(barangList);
            barangTable.setItems(observableList);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data barang!");
            e.printStackTrace();
        }
    }
    
    /**
     * Load instansi list for combo box
     */
    private void loadInstansiList() {
        try {
            Connection conn = com.inventaris.config.DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT i.id_instansi, i.nama_instansi FROM instansi i " +
                         "JOIN user u ON i.id_user = u.id_user " +
                         "WHERE u.status = 'aktif' ORDER BY i.nama_instansi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<String> instansiNames = new ArrayList<>();
            instansiNames.add("Umum"); // Option untuk barang umum
            
            while (rs.next()) {
                instansiNames.add(rs.getString("nama_instansi"));
            }
            
            instansiCombo.setItems(FXCollections.observableArrayList(instansiNames));
            
            // Jika instansi login, auto-select
            if (sessionManager.isInstansi()) {
                String currentInstansi = getCurrentInstansiName();
                instansiCombo.setValue(currentInstansi);
                instansiCombo.setDisable(true); // Ga bisa ganti
            } else {
                instansiCombo.setValue("Umum");
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error loading instansi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
      
    @FXML
    private void handleBarang() {
      
        Main.loadContent("Barang.fxml");
    }
    
    @FXML
    private void handlePeminjaman() {
        System.out.println("➡️ Tombol Peminjaman ditekan");
        Main.loadContent("Peminjaman.fxml");
    }

    
    @FXML
    private void handleLaporan() {
        
        Main.loadContent("Laporan.fxml");
    }
    @FXML
    private void handleHome(){
       Main.loadContent("Home.fxml");
    }
    
    @FXML
    private void handleDashboard(){
       Main.showDashboard();
    }
    @FXML
    private void handleUser() {
      
        Main.loadContent("User.fxml");
    }
    /**
    
    // ============================================================
    // CRUD OPERATIONS
    // ============================================================
    
    /**
     * Handle save button - Create new barang
     */
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            Barang barang = new Barang();
            barang.setKodeBarang(kodeBarangField.getText().trim().toUpperCase());
            barang.setNamaBarang(namaBarangField.getText().trim());
            barang.setLokasiBarang(lokasiField.getText().trim());
            barang.setJumlahTotal(Integer.parseInt(jumlahTotalField.getText()));
            barang.setJumlahTersedia(Integer.parseInt(jumlahTersediaField.getText()));
            barang.setDeskripsi(deskripsiArea.getText().trim());
            barang.setKondisiBarang(kondisiCombo.getValue());
            barang.setStatus(statusCombo.getValue());
            
            // Set instansi pemilik (NEW)
            String instansiName = instansiCombo.getValue();
            Integer instansiId = getInstansiIdByName(instansiName);
            barang.setIdInstansi(instansiId);
            
            if (barangDAO.create(barang)) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil ditambahkan!");
                
                // Log activity
                LogActivityUtil.logCreate(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "barang",
                    barang.getKodeBarang() + " - " + barang.getNamaBarang()
                );
                
                clearForm();
                loadBarangData();
            } else {
                AlertUtil.showError("Gagal", "Gagal menambahkan barang!");
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle update button - Update existing barang
     */
    @FXML
    private void handleUpdate() {
        if (selectedBarang == null) {
            AlertUtil.showWarning("Peringatan", "Pilih barang yang akan diupdate!");
            return;
        }
        
        // Check authorization (NEW)
        if (!canEdit(selectedBarang)) {
            AlertUtil.showWarning("Akses Ditolak", 
                "Anda hanya bisa mengedit barang milik instansi Anda!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        if (!AlertUtil.showConfirmation("Konfirmasi", "Update data barang ini?")) {
            return;
        }
        
        try {
            selectedBarang.setNamaBarang(namaBarangField.getText().trim());
            selectedBarang.setLokasiBarang(lokasiField.getText().trim());
            selectedBarang.setJumlahTotal(Integer.parseInt(jumlahTotalField.getText()));
            selectedBarang.setJumlahTersedia(Integer.parseInt(jumlahTersediaField.getText()));
            selectedBarang.setDeskripsi(deskripsiArea.getText().trim());
            selectedBarang.setKondisiBarang(kondisiCombo.getValue());
            selectedBarang.setStatus(statusCombo.getValue());
            
            // Update instansi (NEW) - hanya admin yang bisa ganti
            if (sessionManager.isAdmin()) {
                String instansiName = instansiCombo.getValue();
                Integer instansiId = getInstansiIdByName(instansiName);
                selectedBarang.setIdInstansi(instansiId);
            }
            
            if (barangDAO.update(selectedBarang)) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil diupdate!");
                
                // Log activity
                LogActivityUtil.logUpdate(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "barang",
                    selectedBarang.getKodeBarang() + " - " + selectedBarang.getNamaBarang()
                );
                
                clearForm();
                loadBarangData();
                setEditMode(false);
            } else {
                AlertUtil.showError("Gagal", "Gagal mengupdate barang!");
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle delete button
     */
    @FXML
    private void handleDelete() {
        if (selectedBarang == null) {
            AlertUtil.showWarning("Peringatan", "Pilih barang yang akan dihapus!");
            return;
        }
        
        // Check authorization (NEW)
        if (!canEdit(selectedBarang)) {
            AlertUtil.showWarning("Akses Ditolak", 
                "Anda hanya bisa menghapus barang milik instansi Anda!");
            return;
        }
        
        if (!AlertUtil.showDeleteConfirmation(selectedBarang.getNamaBarang())) {
            return;
        }
        
        try {
            if (barangDAO.delete(selectedBarang.getKodeBarang())) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil dihapus!");
                
                // Log activity
                LogActivityUtil.logDelete(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "barang",
                    selectedBarang.getKodeBarang() + " - " + selectedBarang.getNamaBarang()
                );
                
                clearForm();
                loadBarangData();
                setEditMode(false);
            } else {
                AlertUtil.showError("Gagal", "Gagal menghapus barang!");
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle search
     */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            loadBarangData();
            return;
        }
        
        try {
            List<Barang> results = barangDAO.search(keyword);
            
            // Filter by instansi if needed
            if (sessionManager.isInstansi()) {
                Integer instansiId = sessionManager.getCurrentRoleId();
                results.removeIf(b -> 
                    b.getIdInstansi() == null || 
                    !b.getIdInstansi().equals(instansiId)
                );
            }
            
            ObservableList<Barang> observableList = FXCollections.observableArrayList(results);
            barangTable.setItems(observableList);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal melakukan pencarian!");
            e.printStackTrace();
        }
    }
    
    // ============================================================
    // FORM MANAGEMENT
    // ============================================================
    
    /**
     * Populate form with selected barang
     */
    private void populateForm(Barang barang) {
        kodeBarangField.setText(barang.getKodeBarang());
        namaBarangField.setText(barang.getNamaBarang());
        lokasiField.setText(barang.getLokasiBarang());
        jumlahTotalField.setText(String.valueOf(barang.getJumlahTotal()));
        jumlahTersediaField.setText(String.valueOf(barang.getJumlahTersedia()));
        deskripsiArea.setText(barang.getDeskripsi());
        kondisiCombo.setValue(barang.getKondisiBarang());
        statusCombo.setValue(barang.getStatus());
        
        // Set instansi combo (NEW)
        if (barang.getNamaPemilik() != null) {
            instansiCombo.setValue(barang.getNamaPemilik());
        } else {
            instansiCombo.setValue("Umum");
        }
    }
    
    /**
     * Clear form
     */
    @FXML
    private void handleClear() {
        clearForm();
        setEditMode(false);
        barangTable.getSelectionModel().clearSelection();
    }
    
    /**
     * Clear all form fields
     */
    private void clearForm() {
        kodeBarangField.clear();
        namaBarangField.clear();
        lokasiField.clear();
        jumlahTotalField.clear();
        jumlahTersediaField.clear();
        deskripsiArea.clear();
        kondisiCombo.setValue("baik");
        statusCombo.setValue("tersedia");
        
        // Reset instansi combo
        if (sessionManager.isInstansi()) {
            instansiCombo.setValue(getCurrentInstansiName());
        } else {
            instansiCombo.setValue("Umum");
        }
        
        selectedBarang = null;
    }
    
    // ============================================================
    // VALIDATION
    // ============================================================
    
    /**
     * Validate input
     */
    private boolean validateInput() {
        if (ValidationUtil.isEmpty(kodeBarangField.getText())) {
            AlertUtil.showWarning("Validasi", "Kode barang tidak boleh kosong!");
            kodeBarangField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidKodeBarang(kodeBarangField.getText())) {
            AlertUtil.showWarning("Validasi", "Format kode barang tidak valid! (A-Z, 0-9, -)");
            kodeBarangField.requestFocus();
            return false;
        }
        
        if (ValidationUtil.isEmpty(namaBarangField.getText())) {
            AlertUtil.showWarning("Validasi", "Nama barang tidak boleh kosong!");
            namaBarangField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isNonNegativeNumber(jumlahTotalField.getText())) {
            AlertUtil.showWarning("Validasi", "Jumlah total harus berupa angka positif!");
            jumlahTotalField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isNonNegativeNumber(jumlahTersediaField.getText())) {
            AlertUtil.showWarning("Validasi", "Jumlah tersedia harus berupa angka positif!");
            jumlahTersediaField.requestFocus();
            return false;
        }
        
        int total = Integer.parseInt(jumlahTotalField.getText());
        int tersedia = Integer.parseInt(jumlahTersediaField.getText());
        
        if (tersedia > total) {
            AlertUtil.showWarning("Validasi", "Jumlah tersedia tidak boleh lebih dari jumlah total!");
            jumlahTersediaField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    // ============================================================
    // AUTHORIZATION HELPERS
    // ============================================================
    
    /**
     * Check if can edit barang
     */
    private boolean canEdit(Barang barang) {
        if (sessionManager.isAdmin()) return true;
        
        if (sessionManager.isInstansi()) {
            Integer instansiId = sessionManager.getCurrentRoleId();
            return barang.getIdInstansi() != null && 
                   barang.getIdInstansi().equals(instansiId);
        }
        
        return false; // Peminjam ga bisa edit
    }
    
    public void searchBarang(String keyword) {
    searchField.setText(keyword);

    try {
        List<Barang> results = barangDAO.search(keyword);

        if (sessionManager.isInstansi()) {
            Integer instansiId = sessionManager.getCurrentRoleId();
            results.removeIf(b -> b.getIdInstansi() == null ||
                                   !b.getIdInstansi().equals(instansiId));
        }

        barangTable.setItems(FXCollections.observableArrayList(results));

    } catch (Exception e) {
        AlertUtil.showError("Error", "Gagal melakukan pencarian!");
        e.printStackTrace();
    }
}



    /**
     * Disable editing for peminjam (read-only)
     */
    private void disableEditingForPeminjam() {
        btnSave.setDisable(true);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        kodeBarangField.setDisable(true);
        namaBarangField.setDisable(true);
        lokasiField.setDisable(true);
        jumlahTotalField.setDisable(true);
        jumlahTersediaField.setDisable(true);
        deskripsiArea.setDisable(true);
        kondisiCombo.setDisable(true);
        statusCombo.setDisable(true);
        instansiCombo.setDisable(true);
    }
    
    /**
     * Set edit mode
     */
    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        kodeBarangField.setDisable(editMode || sessionManager.isPeminjam());
        btnSave.setDisable(editMode || sessionManager.isPeminjam());
        btnUpdate.setDisable(!editMode || sessionManager.isPeminjam());
        btnDelete.setDisable(!editMode || sessionManager.isPeminjam());
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Get current instansi name
     */
    private String getCurrentInstansiName() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            Connection conn = com.inventaris.config.DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT nama_instansi FROM instansi WHERE id_instansi = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instansiId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("nama_instansi");
                rs.close();
                stmt.close();
                conn.close();
                return name;
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get instansi ID by name
     */
    private Integer getInstansiIdByName(String name) {
        if ("Umum".equals(name)) return null;
        
        try {
            Connection conn = com.inventaris.config.DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT id_instansi FROM instansi WHERE nama_instansi = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("id_instansi");
                rs.close();
                stmt.close();
                conn.close();
                return id;
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
}