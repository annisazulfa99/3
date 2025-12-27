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
    
    private Parent currentContent;
    
    // --- Data & Tools ---
    private final BarangDAO barangDAO = new BarangDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private List<Barang> allBarang = new ArrayList<>();
    private List<Barang> filteredBarang = new ArrayList<>();
    
    private String currentSearchKeyword = ""; 
    
    private boolean isUpdatingFilter = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 DataBarangPeminjamController.initialize() started");
        System.out.println("================================================");
        
        // 1. Ambil Data dari Database
        System.out.println("📊 Step 1: Loading data from database...");
        loadAllBarang();
        System.out.println("✅ Step 1 done. Total items: " + allBarang.size());

        // 2. Isi Pilihan Dropdown
        System.out.println("📋 Step 2: Loading filters...");
        loadFilters();
        System.out.println("✅ Step 2 done");
        
        // 3. Pasang Logic "Saling Reset"
        System.out.println("🔗 Step 3: Setting up listeners...");
        setupListeners();
        System.out.println("✅ Step 3 done");
        
        // 4. Pastikan tampilan awal bersih (Semua Barang Tampil)
        System.out.println("🎨 Step 4: Reset filters and display...");
        handleResetFilter(); 
        System.out.println("✅ Step 4 done. Filtered items: " + filteredBarang.size());
        
        // 5. Cek Keranjang
        System.out.println("🛒 Step 5: Update cart badge...");
        updateCartBadge();
        System.out.println("✅ Step 5 done");
        
        System.out.println("================================================");
        System.out.println("✅ DataBarangPeminjamController initialized successfully");
        System.out.println("📦 Total barang: " + allBarang.size());
        System.out.println("🔍 Filtered: " + filteredBarang.size());
        if (catalogGrid != null) {
            System.out.println("🎴 Cards in grid: " + catalogGrid.getChildren().size());
        }
        System.out.println("================================================");
    }
    
    // ============================================================
    // 1. LOGIC FILTER INTERACTION
    // ============================================================
    
    private void setupListeners() {
        if (filterLembaga != null) filterLembaga.setOnAction(e -> handleSingleFilterSelection(filterLembaga));
        if (filterBEM != null) filterBEM.setOnAction(e -> handleSingleFilterSelection(filterBEM));
        if (filterHimpunan != null) filterHimpunan.setOnAction(e -> handleSingleFilterSelection(filterHimpunan));
        if (filterUKM != null) filterUKM.setOnAction(e -> handleSingleFilterSelection(filterUKM));
        
        if (sortCombo != null) sortCombo.setOnAction(e -> applyFiltersAndDisplay());
    }

    private void handleSingleFilterSelection(ComboBox<String> sourceCombo) {
        if (isUpdatingFilter) return; 

        String selectedValue = sourceCombo.getValue();

        if (selectedValue != null && !"Semua".equals(selectedValue)) {
            isUpdatingFilter = true; 
            
            try {
                if (sourceCombo != filterLembaga && filterLembaga != null) filterLembaga.setValue("Semua");
                if (sourceCombo != filterBEM && filterBEM != null) filterBEM.setValue("Semua");
                if (sourceCombo != filterHimpunan && filterHimpunan != null) filterHimpunan.setValue("Semua");
                if (sourceCombo != filterUKM && filterUKM != null) filterUKM.setValue("Semua");
            } finally {
                isUpdatingFilter = false; 
            }
        }
        
        applyFiltersAndDisplay();
    }

    @FXML
    private void handleResetFilter() {
        isUpdatingFilter = true;
        
        try {
            if (filterLembaga != null) filterLembaga.setValue("Semua");
            if (filterBEM != null) filterBEM.setValue("Semua");
            if (filterHimpunan != null) filterHimpunan.setValue("Semua");
            if (filterUKM != null) filterUKM.setValue("Semua");
            
            this.currentSearchKeyword = ""; 
            if (sortCombo != null) sortCombo.setValue("Terbaru");
            
        } finally {
            isUpdatingFilter = false;
        }
        
        applyFiltersAndDisplay();
    }

    // ============================================================
    // 2. DATA PROCESSING & DISPLAY
    // ============================================================

    private void loadAllBarang() {
        try {
            System.out.println("🔍 Fetching barang from DAO...");
            
            allBarang = barangDAO.getAvailable();
            
            System.out.println("📊 DAO returned: " + allBarang.size() + " items");
            
            if (allBarang.isEmpty()) {
                System.err.println("⚠️ WARNING: Database barang kosong atau query gagal!");
                System.err.println("⚠️ Cek apakah:");
                System.err.println("   1. Tabel barang sudah ada data");
                System.err.println("   2. Query getAvailable() berjalan benar");
                System.err.println("   3. Status barang = 'tersedia'");
                System.err.println("   4. Jumlah tersedia > 0");
            } else {
                System.out.println("✅ Sample items loaded:");
                for (int i = 0; i < Math.min(5, allBarang.size()); i++) {
                    Barang b = allBarang.get(i);
                    System.out.println("  " + (i+1) + ". " + b.getNamaBarang() + 
                                     " | Stok: " + b.getJumlahTersedia() +
                                     " | Status: " + b.getStatus() +
                                     " | Pemilik: " + (b.getNamaPemilik() != null ? b.getNamaPemilik() : "Umum"));
                }
            }
            
            if (lblTotalBarang != null) {
                lblTotalBarang.setText(allBarang.size() + " items");
                System.out.println("✅ lblTotalBarang updated: " + allBarang.size() + " items");
            } else {
                System.err.println("⚠️ lblTotalBarang is NULL!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR in loadAllBarang(): " + e.getMessage());
            e.printStackTrace();
            allBarang = new ArrayList<>();
        }
    }

    private void applyFiltersAndDisplay() {
        System.out.println("🔄 applyFiltersAndDisplay() called");
        
        filteredBarang = new ArrayList<>(allBarang);
        InstansiDAO dao = new InstansiDAO();

        // FILTER 1: INSTANSI
        String selectedInstansi = null;

        if (filterLembaga != null && !"Semua".equals(filterLembaga.getValue())) selectedInstansi = filterLembaga.getValue();
        else if (filterBEM != null && !"Semua".equals(filterBEM.getValue())) selectedInstansi = filterBEM.getValue();
        else if (filterHimpunan != null && !"Semua".equals(filterHimpunan.getValue())) selectedInstansi = filterHimpunan.getValue();
        else if (filterUKM != null && !"Semua".equals(filterUKM.getValue())) selectedInstansi = filterUKM.getValue();

        if (selectedInstansi != null) {
            System.out.println("🔍 Filtering by instansi: " + selectedInstansi);
            int id = dao.getIdByNama(selectedInstansi);
            filteredBarang = filteredBarang.stream()
                .filter(b -> b.getIdInstansi() != null && b.getIdInstansi() == id)
                .collect(Collectors.toList());
            System.out.println("📊 After instansi filter: " + filteredBarang.size() + " items");
        }

        // FILTER 2: SEARCH KEYWORD
        if (!currentSearchKeyword.isEmpty()) {
            System.out.println("🔍 Filtering by keyword: " + currentSearchKeyword);
            String lowerKey = currentSearchKeyword.toLowerCase();
            filteredBarang = filteredBarang.stream()
                .filter(b -> b.getNamaBarang().toLowerCase().contains(lowerKey))
                .collect(Collectors.toList());
            System.out.println("📊 After keyword filter: " + filteredBarang.size() + " items");
        }

        // FILTER 3: SORTING
        if (sortCombo != null) {
            String sort = sortCombo.getValue();
            if (sort != null) {
                System.out.println("🔄 Sorting by: " + sort);
                switch (sort) {
                    case "Nama A-Z": filteredBarang.sort(Comparator.comparing(Barang::getNamaBarang)); break;
                    case "Nama Z-A": filteredBarang.sort(Comparator.comparing(Barang::getNamaBarang).reversed()); break;
                    case "Stok Terbanyak": filteredBarang.sort(Comparator.comparingInt(Barang::getJumlahTersedia).reversed()); break;
                }
            }
        }

        displayCatalog();
    }

    private void displayCatalog() {
        System.out.println("🎨 displayCatalog() called");
        System.out.println("------------------------------------------------");
        System.out.println("📦 Items to display: " + filteredBarang.size());
        
        if (catalogGrid == null) {
            System.err.println("❌ ERROR: catalogGrid is NULL!");
            return;
        }
        
        System.out.println("🗑️ Clearing existing cards...");
        catalogGrid.getChildren().clear();
        
        boolean isEmpty = filteredBarang.isEmpty();
        System.out.println("📊 isEmpty: " + isEmpty);
        
        if (emptyState != null) {
            emptyState.setVisible(isEmpty);
            emptyState.setManaged(isEmpty);
            System.out.println("📭 Empty state visibility: " + isEmpty);
        } else {
            System.err.println("⚠️ emptyState is NULL!");
        }
        
        if (lblResultCount != null) {
            lblResultCount.setText("Menampilkan " + filteredBarang.size() + " barang");
            System.out.println("🏷️ Result count updated: " + filteredBarang.size());
        } else {
            System.err.println("⚠️ lblResultCount is NULL!");
        }

        if (!isEmpty) {
            System.out.println("🎴 Creating " + filteredBarang.size() + " cards...");
            
            for (int i = 0; i < filteredBarang.size(); i++) {
                Barang barang = filteredBarang.get(i);
                System.out.println("  Creating card " + (i+1) + ": " + barang.getNamaBarang());
                
                try {
                    VBox card = createBarangCard(barang);
                    catalogGrid.getChildren().add(card);
                    System.out.println("  ✅ Card added to grid");
                } catch (Exception e) {
                    System.err.println("  ❌ Failed to create card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("✅ All cards created successfully");
            System.out.println("🎴 Total cards in grid: " + catalogGrid.getChildren().size());
        } else {
            System.out.println("⚠️ No items to display - showing empty state");
        }
        
        System.out.println("------------------------------------------------");
    }

    // ============================================================
    // 3. UTILITIES & INITIAL DATA SETUP
    // ============================================================

    private void loadFilters() {
        InstansiDAO dao = new InstansiDAO();
        if (filterLembaga != null) fillCombo(filterLembaga, dao.getByKategori("LEMBAGA"));
        if (filterBEM != null) fillCombo(filterBEM, dao.getByKategori("BEM"));
        if (filterHimpunan != null) fillCombo(filterHimpunan, dao.getByKategori("HIMPUNAN"));
        if (filterUKM != null) fillCombo(filterUKM, dao.getByKategori("UKM"));

        if (sortCombo != null) {
            sortCombo.getItems().setAll("Terbaru", "Nama A-Z", "Nama Z-A", "Stok Terbanyak");
            sortCombo.setValue("Terbaru");
        }
    }

    private void fillCombo(ComboBox<String> combo, List<String> items) {
        combo.getItems().clear();
        combo.getItems().add("Semua");
        if (items != null) combo.getItems().addAll(items);
        combo.setValue("Semua");
    }

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

        Label nameLbl = new Label(barang.getNamaBarang());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLbl.setWrapText(true); 
        nameLbl.setAlignment(Pos.CENTER);

        Label stokLbl = new Label("Stok: " + barang.getJumlahTersedia());
        stokLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label ownerLbl = new Label(barang.getNamaPemilik() != null ? barang.getNamaPemilik() : "Umum");
        ownerLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6A5436;");

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
        }
    }
    
    @FXML 
    private void handleApplyFilter() { 
        applyFiltersAndDisplay(); 
    }
}
