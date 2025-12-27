// ================================================================
// File: src/main/java/com/inventaris/controller/BarangInstansiController.java
// Controller untuk CRUD Barang Instansi (tampilan katalog)
// ================================================================
package com.inventaris.controller;

import com.inventaris.dao.BarangDAO;
import com.inventaris.model.Barang;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import com.inventaris.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * BarangInstansiController - CRUD Barang untuk Instansi
 * Tampilan katalog dengan form CRUD di sidebar
 */
public class BarangInstansiController implements Initializable {
    
    // Form Fields
    @FXML private TextField kodeBarangField;
    @FXML private TextField namaBarangField;
    @FXML private TextField lokasiField;
    @FXML private TextField jumlahTotalField;
    @FXML private TextField jumlahTersediaField;
    @FXML private TextArea deskripsiArea;
    @FXML private ComboBox<String> kondisiCombo;
    @FXML private ComboBox<String> statusCombo;
    
    // Buttons
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    
    // Catalog
    @FXML private FlowPane catalogGrid;
    @FXML private VBox emptyState;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Label lblResultCount;
    @FXML private Label lblTotalBarang;
    
    // Data
    private final BarangDAO barangDAO = new BarangDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private List<Barang> allBarang;
    private Barang selectedBarang;
    private boolean isEditMode = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Security check
        if (!sessionManager.isInstansi()) {
            AlertUtil.showError("Access Denied", "Halaman ini khusus untuk Instansi!");
            return;
        }
        
        // Initialize ComboBoxes
        kondisiCombo.getItems().addAll("baik", "rusak ringan", "rusak berat");
        kondisiCombo.setValue("baik");
        
        statusCombo.getItems().addAll("tersedia", "dipinjam", "rusak", "hilang");
        statusCombo.setValue("tersedia");
        
        sortCombo.getItems().addAll("Terbaru", "Nama A-Z", "Nama Z-A", "Stok Terbanyak");
        sortCombo.setValue("Terbaru");
        sortCombo.setOnAction(e -> displayCatalog());
        
        // Load data
        loadBarangData();
        
        // Set initial state
        setEditMode(false);
        
        System.out.println("✅ BarangInstansi Controller initialized");
    }
    
    // ============================================================
    // DATA LOADING
    // ============================================================
    
    private void loadBarangData() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            allBarang = barangDAO.getByInstansi(instansiId);
            
            lblTotalBarang.setText(String.valueOf(allBarang.size()));
            displayCatalog();
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data barang!");
            e.printStackTrace();
        }
    }
    
    private void displayCatalog() {
        catalogGrid.getChildren().clear();
        
        // Apply sorting
        List<Barang> sortedList = allBarang;
        String sort = sortCombo.getValue();
        
        if (sort != null) {
            switch (sort) {
                case "Nama A-Z":
                    sortedList.sort(Comparator.comparing(Barang::getNamaBarang));
                    break;
                case "Nama Z-A":
                    sortedList.sort(Comparator.comparing(Barang::getNamaBarang).reversed());
                    break;
                case "Stok Terbanyak":
                    sortedList.sort(Comparator.comparingInt(Barang::getJumlahTersedia).reversed());
                    break;
                // "Terbaru" - keep default order
            }
        }
        
        // Show/hide empty state
        boolean isEmpty = sortedList.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
        
        lblResultCount.setText("Menampilkan " + sortedList.size() + " barang");
        
        // Create cards
        for (Barang barang : sortedList) {
            catalogGrid.getChildren().add(createBarangCard(barang));
        }
    }
    
    private VBox createBarangCard(Barang barang) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(220, 320);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-padding: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
            "-fx-cursor: hand;"
        );
        
        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        
        try {
            String path = barang.getFotoUrl();
            if (path != null && !path.isBlank()) {
                if (path.startsWith("http")) {
                    imageView.setImage(new Image(path, true));
                } else {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is != null) imageView.setImage(new Image(is));
                }
            }
        } catch (Exception ignored) {}
        
        if (imageView.getImage() == null) {
            InputStream ph = getClass().getResourceAsStream("/images/barang/placeholder.png");
            if (ph != null) imageView.setImage(new Image(ph));
        }
        
        // Labels
        Label nameLbl = new Label(barang.getNamaBarang());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLbl.setWrapText(true);
        nameLbl.setAlignment(Pos.CENTER);
        
        Label kodeLbl = new Label("Kode: " + barang.getKodeBarang());
        kodeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        Label stokLbl = new Label("Stok: " + barang.getJumlahTersedia() + "/" + barang.getJumlahTotal());
        stokLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label statusLbl = new Label(barang.getStatus().toUpperCase());
        statusLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        
        // Status color
        switch (barang.getStatus().toLowerCase()) {
            case "tersedia":
                statusLbl.setStyle(statusLbl.getStyle() + "-fx-text-fill: green;");
                break;
            case "dipinjam":
                statusLbl.setStyle(statusLbl.getStyle() + "-fx-text-fill: orange;");
                break;
            default:
                statusLbl.setStyle(statusLbl.getStyle() + "-fx-text-fill: red;");
        }
        
        // Edit button
        Button btnEdit = new Button("✏️ Edit");
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        btnEdit.setStyle(
            "-fx-background-color: #6A5436; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-font-weight: bold;"
        );
        btnEdit.setOnAction(e -> selectBarangForEdit(barang));
        
        card.getChildren().addAll(imageView, nameLbl, kodeLbl, stokLbl, statusLbl, btnEdit);
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                card.getStyle() + 
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); " +
                "-fx-scale-x: 1.02; " +
                "-fx-scale-y: 1.02;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                "-fx-cursor: hand;"
            );
        });
        
        return card;
    }
    
    // ============================================================
    // CRUD OPERATIONS
    // ============================================================
    
    @FXML
    private void handleSave() {
        if (!validateInput()) return;
        
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            
            Barang barang = new Barang();
            barang.setKodeBarang(kodeBarangField.getText().trim().toUpperCase());
            barang.setNamaBarang(namaBarangField.getText().trim());
            barang.setLokasiBarang(lokasiField.getText().trim());
            barang.setJumlahTotal(Integer.parseInt(jumlahTotalField.getText()));
            barang.setJumlahTersedia(Integer.parseInt(jumlahTersediaField.getText()));
            barang.setDeskripsi(deskripsiArea.getText().trim());
            barang.setKondisiBarang(kondisiCombo.getValue());
            barang.setStatus(statusCombo.getValue());
            barang.setIdInstansi(instansiId);
            
            if (barangDAO.create(barang)) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil ditambahkan!");
                
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
    
    @FXML
    private void handleUpdate() {
        if (selectedBarang == null) {
            AlertUtil.showWarning("Peringatan", "Pilih barang yang akan diupdate!");
            return;
        }
        
        if (!validateInput()) return;
        
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
            
            if (barangDAO.update(selectedBarang)) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil diupdate!");
                
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
    
    @FXML
    private void handleDelete() {
        if (selectedBarang == null) {
            AlertUtil.showWarning("Peringatan", "Pilih barang yang akan dihapus!");
            return;
        }
        
        if (!AlertUtil.showDeleteConfirmation(selectedBarang.getNamaBarang())) {
            return;
        }
        
        try {
            if (barangDAO.delete(selectedBarang.getKodeBarang())) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil dihapus!");
                
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
    
    @FXML
    private void handleClear() {
        clearForm();
        setEditMode(false);
    }
    
    // ============================================================
    // FORM MANAGEMENT
    // ============================================================
    
    private void selectBarangForEdit(Barang barang) {
        selectedBarang = barang;
        populateForm(barang);
        setEditMode(true);
        
        // Scroll to top (optional)
        System.out.println("✏️ Editing: " + barang.getNamaBarang());
    }
    
    private void populateForm(Barang barang) {
        kodeBarangField.setText(barang.getKodeBarang());
        namaBarangField.setText(barang.getNamaBarang());
        lokasiField.setText(barang.getLokasiBarang());
        jumlahTotalField.setText(String.valueOf(barang.getJumlahTotal()));
        jumlahTersediaField.setText(String.valueOf(barang.getJumlahTersedia()));
        deskripsiArea.setText(barang.getDeskripsi());
        kondisiCombo.setValue(barang.getKondisiBarang());
        statusCombo.setValue(barang.getStatus());
    }
    
    private void clearForm() {
        kodeBarangField.clear();
        namaBarangField.clear();
        lokasiField.clear();
        jumlahTotalField.clear();
        jumlahTersediaField.clear();
        deskripsiArea.clear();
        kondisiCombo.setValue("baik");
        statusCombo.setValue("tersedia");
        selectedBarang = null;
    }
    
    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        kodeBarangField.setDisable(editMode);
        btnSave.setDisable(editMode);
        btnUpdate.setDisable(!editMode);
        btnDelete.setDisable(!editMode);
    }
    
    // ============================================================
    // VALIDATION
    // ============================================================
    
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
}