package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.dao.BarangDAO;
import com.inventaris.dao.InstansiDAO;
import com.inventaris.model.Barang;
import com.inventaris.model.CartItem;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.CartManager;
import com.inventaris.util.SessionManager;
import java.io.IOException;
import java.io.InputStream;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


public class DataBarangPeminjamController implements Initializable {
    
    // --- UI Component ---
    @FXML private Label cartBadge;
    @FXML private Label lblTotalBarang;
    @FXML private Label lblResultCount;
    @FXML private ComboBox<String> filterLembaga;
    @FXML private ComboBox<String> filterBEM;
    @FXML private ComboBox<String> filterHimpunan;
    @FXML private ComboBox<String> filterUKM;
    @FXML private ComboBox<String> sortCombo;
    @FXML private FlowPane catalogGrid;
    @FXML private VBox emptyState;
    @FXML private StackPane contentArea;
        // --- Utilities ---


    private Parent currentContent;
    // --- Data & Tools ---
    private final BarangDAO barangDAO = new BarangDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private List<Barang> allBarang = new ArrayList<>();
    private List<Barang> filteredBarang = new ArrayList<>();
    
    // Keyword pencarian dari LayoutController
    private String currentSearchKeyword = ""; 
    
    // 🛡️ FLAG PENGAMAN: Mencegah error looping saat reset otomatis
    private boolean isUpdatingFilter = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Ambil Data dari Database
        loadAllBarang();

        // 2. Isi Pilihan Dropdown
        loadFilters();
        
        // 3. Pasang Logic "Saling Reset"
        setupListeners();
        
        // 4. Pastikan tampilan awal bersih (Semua Barang Tampil)
        handleResetFilter(); 
        
        // 5. Cek Keranjang
        updateCartBadge();
        
        System.out.println("✅ DataBarang Initialized. Total: " + allBarang.size());
    }
    
    // ============================================================
    // 1. LOGIC FILTER INTERACTION (INTI PERUBAHAN)
    // ============================================================
    
    private void setupListeners() {
        // Pasang listener khusus ke setiap ComboBox
        filterLembaga.setOnAction(e -> handleSingleFilterSelection(filterLembaga));
        filterBEM.setOnAction(e -> handleSingleFilterSelection(filterBEM));
        filterHimpunan.setOnAction(e -> handleSingleFilterSelection(filterHimpunan));
        filterUKM.setOnAction(e -> handleSingleFilterSelection(filterUKM));
        
        // Listener Sort beda sendiri (tidak mereset filter lain)
        sortCombo.setOnAction(e -> applyFiltersAndDisplay());
    }

    /**
     * Logic Pintar: Saat satu dipilih, yang lain otomatis jadi "Semua"
     */
    private void handleSingleFilterSelection(ComboBox<String> sourceCombo) {
        // Jika sedang proses reset, jangan jalankan logic ini (biar ga crash/looping)
        if (isUpdatingFilter) return; 

        String selectedValue = sourceCombo.getValue();

        // Jika user memilih sesuatu yang BUKAN "Semua"
        if (selectedValue != null && !"Semua".equals(selectedValue)) {
            
            // 🔒 Kunci pintu dulu
            isUpdatingFilter = true; 
            
            try {
                // Reset ComboBox lain selain yang sedang dipilih
                if (sourceCombo != filterLembaga) filterLembaga.setValue("Semua");
                if (sourceCombo != filterBEM) filterBEM.setValue("Semua");
                if (sourceCombo != filterHimpunan) filterHimpunan.setValue("Semua");
                if (sourceCombo != filterUKM) filterUKM.setValue("Semua");
            } finally {
                // 🔓 Buka kunci pintu
                isUpdatingFilter = false; 
            }
        }
        
        // Terapkan filter ke layar
        applyFiltersAndDisplay();
    }

    @FXML
    private void handleResetFilter() {
        // 🔒 Kunci pintu agar listener di atas tidak 'kaget' saat kita ubah paksa
        isUpdatingFilter = true;
        
        try {
            filterLembaga.setValue("Semua");
            filterBEM.setValue("Semua");
            filterHimpunan.setValue("Semua");
            filterUKM.setValue("Semua");
            
            this.currentSearchKeyword = ""; 
            sortCombo.setValue("Terbaru");
            
        } finally {
            // 🔓 Buka kunci
            isUpdatingFilter = false;
        }
        
        // Tampilkan ulang semua data
        applyFiltersAndDisplay();
    }

    // ============================================================
    // 2. DATA PROCESSING & DISPLAY
    // ============================================================

    private void loadAllBarang() {
        try {
            allBarang = barangDAO.getAvailable();
            lblTotalBarang.setText(allBarang.size() + " items");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFiltersAndDisplay() {
        filteredBarang = new ArrayList<>(allBarang); // Reset list ke full dulu
        InstansiDAO dao = new InstansiDAO();

        // --- FILTER 1: INSTANSI ---
        // Cari mana ComboBox yang sedang AKTIF (Tidak "Semua")
        String selectedInstansi = null;

        if (!"Semua".equals(filterLembaga.getValue())) selectedInstansi = filterLembaga.getValue();
        else if (!"Semua".equals(filterBEM.getValue())) selectedInstansi = filterBEM.getValue();
        else if (!"Semua".equals(filterHimpunan.getValue())) selectedInstansi = filterHimpunan.getValue();
        else if (!"Semua".equals(filterUKM.getValue())) selectedInstansi = filterUKM.getValue();

        // Jika ada yang dipilih, Filter list berdasarkan ID Instansi tersebut
        if (selectedInstansi != null) {
            int id = dao.getIdByNama(selectedInstansi);
            filteredBarang = filteredBarang.stream()
                .filter(b -> b.getIdInstansi() != null && b.getIdInstansi() == id)
                .collect(Collectors.toList());
        }

        // --- FILTER 2: SEARCH KEYWORD (Dari LayoutController) ---
        if (!currentSearchKeyword.isEmpty()) {
            String lowerKey = currentSearchKeyword.toLowerCase();
            filteredBarang = filteredBarang.stream()
                .filter(b -> b.getNamaBarang().toLowerCase().contains(lowerKey))
                .collect(Collectors.toList());
        }

        // --- FILTER 3: SORTING ---
        String sort = sortCombo.getValue();
        if (sort != null) {
            switch (sort) {
                case "Nama A-Z": filteredBarang.sort(Comparator.comparing(Barang::getNamaBarang)); break;
                case "Nama Z-A": filteredBarang.sort(Comparator.comparing(Barang::getNamaBarang).reversed()); break;
                case "Stok Terbanyak": filteredBarang.sort(Comparator.comparingInt(Barang::getJumlahTersedia).reversed()); break;
                // "Terbaru" biarkan default urutan database
            }
        }

        // Render ke Layar
        displayCatalog();
    }

    private void displayCatalog() {
        catalogGrid.getChildren().clear();
        boolean isEmpty = filteredBarang.isEmpty();
        
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty); // Agar tidak makan tempat jika false
        
        lblResultCount.setText("Menampilkan " + filteredBarang.size() + " barang");

        if (!isEmpty) {
            for (Barang barang : filteredBarang) {
                catalogGrid.getChildren().add(createBarangCard(barang));
            }
        }
    }

    // ============================================================
    // 3. UTILITIES & INITIAL DATA SETUP
    // ============================================================

    private void loadFilters() {
        InstansiDAO dao = new InstansiDAO();
        fillCombo(filterLembaga, dao.getByKategori("LEMBAGA"));
        fillCombo(filterBEM, dao.getByKategori("BEM"));
        fillCombo(filterHimpunan, dao.getByKategori("HIMPUNAN"));
        fillCombo(filterUKM, dao.getByKategori("UKM"));

        sortCombo.getItems().setAll("Terbaru", "Nama A-Z", "Nama Z-A", "Stok Terbanyak");
        sortCombo.setValue("Terbaru");
    }

    private void fillCombo(ComboBox<String> combo, List<String> items) {
        combo.getItems().clear();
        combo.getItems().add("Semua"); // Default option
        if (items != null) combo.getItems().addAll(items);
        combo.setValue("Semua"); // Set default value agar tidak null
    }

    // Dipanggil oleh LayoutController (Search Header)
    public void searchBarang(String keyword) {
        this.currentSearchKeyword = (keyword == null) ? "" : keyword.trim();
        applyFiltersAndDisplay();
    }

    // ============================================================
    // 4. UI COMPONENTS (CARD & CART)
    // ============================================================

    private VBox createBarangCard(Barang barang) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(220, 320);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Image Handling
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180); imageView.setFitHeight(140); imageView.setPreserveRatio(true);
        
        try {
            String path = barang.getFotoUrl();
            if (path != null && !path.isBlank()) {
                if (path.startsWith("http")) imageView.setImage(new Image(path, true));
                else {
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
        nameLbl.setWrapText(true); nameLbl.setAlignment(Pos.CENTER);

        Label stokLbl = new Label("Stok: " + barang.getJumlahTersedia());
        stokLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label ownerLbl = new Label(barang.getNamaPemilik() != null ? barang.getNamaPemilik() : "Umum");
        ownerLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6A5436;");

        // Button
        Button btnAdd = new Button("+ Keranjang");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setStyle("-fx-background-color: #6A5436; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> showAddToCartDialog(barang));

        card.getChildren().addAll(imageView, nameLbl, stokLbl, ownerLbl, btnAdd);
        return card;
    }

    private void showAddToCartDialog(Barang barang) {
        Dialog<CartItem> dialog = new Dialog<>();
        dialog.setTitle("Tambah Keranjang");
        dialog.setHeaderText(barang.getNamaBarang());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        HBox boxJumlah = new HBox(10, new Label("Jumlah:"));
        Spinner<Integer> spinner = new Spinner<>(1, barang.getJumlahTersedia(), 1);
        spinner.setEditable(true);
        boxJumlah.getChildren().add(spinner);
        boxJumlah.setAlignment(Pos.CENTER_LEFT);

        DatePicker dpPinjam = new DatePicker(LocalDate.now());
        DatePicker dpKembali = new DatePicker(LocalDate.now().plusDays(7));
        
        VBox dateBox = new VBox(10, 
            new HBox(10, new Label("Tgl Pinjam: "), dpPinjam),
            new HBox(10, new Label("Tgl Kembali:"), dpKembali)
        );

        content.getChildren().addAll(boxJumlah, dateBox);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                CartItem item = new CartItem();
                item.setBarang(barang);
                item.setJumlahPinjam(spinner.getValue());
                item.setTglPinjam(dpPinjam.getValue());
                item.setTglKembali(dpKembali.getValue());
                return item;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::addToCart);
    }

    private void addToCart(CartItem item) {
        if (!item.isValid()) {
            AlertUtil.showWarning("Validasi", item.getValidationError());
            return;
        }
        if (CartManager.getInstance().hasBarang(item.getBarang().getIdBarang())) {
            AlertUtil.showWarning("Info", "Barang sudah ada di keranjang.");
            return;
        }
        CartManager.getInstance().addItem(item);
        updateCartBadge();
        AlertUtil.showSuccess("Sukses", "Masuk keranjang!");
    }
    
   

private static LayoutController layoutController;
    
        @FXML
    public void handlePeminjaman() {
        // Panggil langsung ke pusat navigasi
        if (LayoutController.getInstance() != null) {
            LayoutController.getInstance().handlePeminjaman();
        }
    }
    private void updateCartBadge() {
        if (cartBadge != null) {
            int count = CartManager.getInstance().getCart().size();
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(count > 0);
        }
    }
    
   

 private void loadPage(String fxmlPath) {
        try {
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            currentContent = loader.load();
            contentArea.getChildren().add(currentContent);
        } catch (IOException e) {
            System.err.println("❌ Gagal memuat halaman: " + fxmlPath);
            e.printStackTrace();
            // Opsional: Tampilkan pesan error di UI jika file tidak ditemukan
        }
    }
  @FXML private void handleApplyFilter() { applyFiltersAndDisplay(); }
    }
