package com.inventaris.controller;

import com.inventaris.dao.BorrowDAO;
import com.inventaris.dao.BarangDAO;
import com.inventaris.model.Borrow;
import com.inventaris.model.CartItem;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.CartManager;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class PeminjamanPeminjamController implements Initializable {
    
    // --- UI ELEMENTS ---
    @FXML private ToggleButton tabPengajuan;
    @FXML private ToggleButton tabSedang;
    @FXML private ToggleButton tabRiwayat;
    
    @FXML private VBox contentPengajuan;
    @FXML private VBox contentSedang;
    @FXML private VBox contentRiwayat;
    
    // --- TAB 1: PENGAJUAN (KERANJANG) ---
    @FXML private TextField txtLampiranSurat; 
    @FXML private Label lblTotalItems;
    @FXML private TableView<CartItemDisplay> tablePengajuan;
    @FXML private TableColumn<CartItemDisplay, Integer> colPengajuanNo;
    @FXML private TableColumn<CartItemDisplay, String> colPengajuanNama;
    @FXML private TableColumn<CartItemDisplay, String> colPengajuanPemilik;
    @FXML private TableColumn<CartItemDisplay, Integer> colPengajuanJumlah;
    @FXML private TableColumn<CartItemDisplay, LocalDate> colPengajuanTglPinjam;
    @FXML private TableColumn<CartItemDisplay, LocalDate> colPengajuanTglKembali;
    @FXML private TableColumn<CartItemDisplay, Void> colPengajuanAction;
    
    // --- TAB 2: SEDANG DIPINJAM (AKTIF) ---
    @FXML private TableView<Borrow> tableSedang;
    @FXML private TableColumn<Borrow, Integer> colSedangId;
    @FXML private TableColumn<Borrow, String> colSedangNama;
    @FXML private TableColumn<Borrow, String> colSedangPemilik; // Optional jika di FXML ada
    @FXML private TableColumn<Borrow, Integer> colSedangJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colSedangTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colSedangDeadline;
    @FXML private TableColumn<Borrow, Long> colSedangSisa; // Untuk Sisa Hari
    @FXML private TableColumn<Borrow, Void> colSedangAction; // Tombol Kembali
    
    // --- TAB 3: RIWAYAT ---
    @FXML private TableView<Borrow> tableRiwayat;
    @FXML private TableColumn<Borrow, Integer> colRiwayatId;
    @FXML private TableColumn<Borrow, String> colRiwayatNama;
    @FXML private TableColumn<Borrow, String> colRiwayatPemilik;
    @FXML private TableColumn<Borrow, Integer> colRiwayatJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colRiwayatTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colRiwayatTglKembali;
    @FXML private TableColumn<Borrow, String> colRiwayatStatus; // Warna status

    // --- LOGIC TOOLS ---
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final BarangDAO barangDAO = new BarangDAO();
    private List<CartItem> cart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. AMBIL DATA KERANJANG
        this.cart = CartManager.getInstance().getCart();
        
        // 2. SETUP TABEL (Semua Tab)
        setupPengajuanTable();
        setupSedangTable();
        setupRiwayatTable();
        
        if (tableSedang != null) {
        tableSedang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    if (tableRiwayat != null) {
        tableRiwayat.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
        
        // 3. LOAD DATA AWAL
        loadPengajuanData();
        
        // 4. BUKA TAB DEFAULT
        showPengajuanTab();
        
        System.out.println("✅ Peminjaman Controller Siap.");
    }

    // ============================================================
    // NAVIGATION TABS
    // ============================================================
    
    @FXML
    private void handleTabChange() {
        if (tabPengajuan.isSelected()) {
            showPengajuanTab();
        } else if (tabSedang.isSelected()) {
            showSedangTab();
        } else if (tabRiwayat.isSelected()) {
            showRiwayatTab();
        }
    }

    private void showPengajuanTab() {
        setVisibleTab(contentPengajuan, tabPengajuan);
        loadPengajuanData();
    }
    
    private void showSedangTab() {
        setVisibleTab(contentSedang, tabSedang);
        loadSedangData(); // Load dari Database
    }
    
    private void showRiwayatTab() {
        setVisibleTab(contentRiwayat, tabRiwayat);
        loadRiwayatData(); // Load History
    }

    private void setVisibleTab(VBox content, ToggleButton tab) {
        contentPengajuan.setVisible(false); contentPengajuan.setManaged(false);
        contentSedang.setVisible(false); contentSedang.setManaged(false);
        contentRiwayat.setVisible(false); contentRiwayat.setManaged(false);
        
        String inactiveStyle = "-fx-background-color: #D9CBC1; -fx-text-fill: black; -fx-cursor: hand;";
        tabPengajuan.setStyle(inactiveStyle);
        tabSedang.setStyle(inactiveStyle);
        tabRiwayat.setStyle(inactiveStyle);
        
        if (content != null) { content.setVisible(true); content.setManaged(true); }
        if (tab != null) { tab.setStyle("-fx-background-color: #8C6E63; -fx-text-fill: white; -fx-font-weight: bold;"); }
    }
    
    
    
    private void setupSedangTable() {
        if(colSedangId != null) colSedangId.setCellValueFactory(new PropertyValueFactory<>("idPeminjaman"));
        colSedangNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colSedangJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colSedangTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colSedangDeadline.setCellValueFactory(new PropertyValueFactory<>("dlKembali"));
        
        // --- [UPDATE 1] CONFIG SISA HARI / STATUS ---
        // Pastikan kita mengambil nilai sisa hari dari object Borrow
        // Asumsi di class Borrow ada method getSisaHari()
        colSedangSisa.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSisaHari())
        );

        // Custom Cell Factory untuk Mengubah Teks Berdasarkan Status
        colSedangSisa.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Long sisaHari, boolean empty) {
                super.updateItem(sisaHari, empty);
                
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    // Ambil data baris saat ini
                    Borrow b = getTableView().getItems().get(getIndex());
                    String status = b.getStatusBarang();

                    if ("pending".equalsIgnoreCase(status)) {
                        // KONDISI 1: BELUM DISETUJUI
                        setText("⏳ Menunggu Persetujuan");
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold; -fx-font-style: italic;"); // Warna Orange
                        
                    } else if ("dipinjam".equalsIgnoreCase(status)) {
                        // KONDISI 2: SUDAH DISETUJUI (Tampilkan Sisa Hari)
                        if (sisaHari == null) sisaHari = 0L;
                        
                        if (sisaHari < 0) {
                            setText("⚠️ Terlambat (" + Math.abs(sisaHari) + " hari)");
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else {
                            setText("✅ Disetujui (Sisa " + sisaHari + " hari)");
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        }
                    } else {
                        setText(status);
                        setStyle("");
                    }
                }
            }
        });

        // --- [UPDATE 2] TOMBOL AKSI (SINKRONISASI) ---
        colSedangAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnKembali = new Button("Kembalikan");
            {
                btnKembali.setStyle("-fx-background-color: #6A5436; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnKembali.setOnAction(e -> handleKembalikan(getTableView().getItems().get(getIndex())));
            }
            
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { 
                    setGraphic(null); 
                } else {
                    Borrow b = getTableView().getItems().get(getIndex());
                    
                    // TOMBOL HANYA MUNCUL JIKA STATUS 'DIPINJAM' (Disetujui)
                    // Ini otomatis sinkron dengan logika kolom Status di atas
                    if ("dipinjam".equalsIgnoreCase(b.getStatusBarang())) {
                        setGraphic(btnKembali);
                    } else {
                        // Jika masih pending, tombol tidak muncul
                        setGraphic(null);
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    // ============================================================
    // TAB 1: LOGIKA PENGAJUAN (CART)
    // ============================================================

    @FXML
    private void handleSubmitPengajuan() {
        if (cart.isEmpty()) {
            AlertUtil.showWarning("Keranjang Kosong", "Silakan pilih barang dulu.");
            return;
        }

        if (txtLampiranSurat != null && txtLampiranSurat.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validasi", "Harap lampirkan keterangan/surat!");
            return;
        }

        if (!AlertUtil.showConfirmation("Konfirmasi", "Ajukan peminjaman " + cart.size() + " barang ini?")) return;

        Integer peminjamId = sessionManager.getCurrentRoleId();
        if (peminjamId == null) return;

        int successCount = 0;
        for (CartItem item : cart) {
            Borrow borrow = new Borrow();
            borrow.setIdPeminjam(peminjamId);
            borrow.setKodeBarang(item.getBarang().getKodeBarang());
            borrow.setJumlahPinjam(item.getJumlahPinjam());
            borrow.setTglPeminjaman(LocalDate.now());
            borrow.setTglPinjam(item.getTglPinjam());
            borrow.setDlKembali(item.getTglKembali());
            borrow.setStatusBarang("pending");
            
            if (borrowDAO.create(borrow)) {
                successCount++;
                LogActivityUtil.logCreate(sessionManager.getCurrentUsername(), sessionManager.getCurrentRole(), "peminjaman", item.getBarang().getNamaBarang());
            }
        }

        if (successCount > 0) {
            AlertUtil.showSuccess("Berhasil", successCount + " barang diajukan.");
            cart.clear();
            CartManager.getInstance().clearCart();
            if (txtLampiranSurat != null) txtLampiranSurat.clear();
            loadPengajuanData();
            
            // Otomatis pindah ke tab Sedang Dipinjam agar user lihat status pending
            tabSedang.setSelected(true);
            showSedangTab();
        } else {
            AlertUtil.showError("Gagal", "Terjadi kesalahan.");
        }
    }

    private void loadPengajuanData() {
        ObservableList<CartItemDisplay> displayItems = FXCollections.observableArrayList();
        AtomicInteger counter = new AtomicInteger(1);
        for (CartItem item : cart) {
            displayItems.add(new CartItemDisplay(counter.getAndIncrement(), item));
        }
        tablePengajuan.setItems(displayItems);
        if (lblTotalItems != null) lblTotalItems.setText(cart.size() + " barang");
    }

    private void setupPengajuanTable() {
        colPengajuanNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colPengajuanNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colPengajuanPemilik.setCellValueFactory(new PropertyValueFactory<>("pemilik"));
        colPengajuanJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colPengajuanTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colPengajuanTglKembali.setCellValueFactory(new PropertyValueFactory<>("tglKembali"));
        
        colPengajuanAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnDelete = new Button("❌");
            {
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand; -fx-font-weight: bold;");
                btnDelete.setOnAction(e -> {
                    CartItemDisplay item = getTableView().getItems().get(getIndex());
                    CartManager.getInstance().removeItem(item.getOriginalItem());
                    loadPengajuanData();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
                setAlignment(Pos.CENTER);
            }
        });
    }

    // ============================================================
    // TAB 2: LOGIKA SEDANG DIPINJAM (ACTIVE)
    // ============================================================

    private void loadSedangData() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) return;
            
            // Ambil semua milik user, lalu filter yg statusnya 'dipinjam' atau 'pending'
            List<Borrow> borrows = borrowDAO.getByPeminjamId(peminjamId);
            borrows.removeIf(b -> !("dipinjam".equals(b.getStatusBarang()) || "pending".equals(b.getStatusBarang())));
            
            tableSedang.setItems(FXCollections.observableArrayList(borrows));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    
    private void handleKembalikan(Borrow borrow) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Kembalikan Barang");
        dialog.setHeaderText("Kembalikan: " + borrow.getNamaBarang());
        
        ComboBox<String> kondisiCombo = new ComboBox<>();
        kondisiCombo.getItems().addAll("baik", "rusak ringan", "rusak berat", "hilang");
        kondisiCombo.setValue("baik");
        
        VBox content = new VBox(10, new Label("Kondisi Barang:"), kondisiCombo);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (borrowDAO.returnItem(borrow.getIdPeminjaman(), kondisiCombo.getValue(), null)) {
                    AlertUtil.showSuccess("Berhasil", "Barang dikembalikan.");
                    loadSedangData(); // Refresh table
                } else {
                    AlertUtil.showError("Gagal", "Gagal mengembalikan barang.");
                }
            }
        });
    }

    // ============================================================
    // TAB 3: LOGIKA RIWAYAT
    // ============================================================

    private void loadRiwayatData() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) return;
            
            List<Borrow> borrows = borrowDAO.getByPeminjamId(peminjamId);
            // Hanya ambil yang sudah selesai (dikembalikan/hilang/rusak/ditolak)
            borrows.removeIf(b -> "dipinjam".equals(b.getStatusBarang()) || "pending".equals(b.getStatusBarang()));
            
            tableRiwayat.setItems(FXCollections.observableArrayList(borrows));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupRiwayatTable() {
        if(colRiwayatId != null) colRiwayatId.setCellValueFactory(new PropertyValueFactory<>("idPeminjaman"));
        colRiwayatNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colRiwayatJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colRiwayatTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colRiwayatTglKembali.setCellValueFactory(new PropertyValueFactory<>("tglKembali"));
        colRiwayatStatus.setCellValueFactory(new PropertyValueFactory<>("statusBarang"));
        
        // Warna Status Riwayat
        colRiwayatStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setStyle("");
                } else {
                    setText(status.toUpperCase());
                    if ("dikembalikan".equalsIgnoreCase(status)) setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    else if ("ditolak".equalsIgnoreCase(status)) setStyle("-fx-text-fill: red;");
                    else setStyle("-fx-text-fill: #b45309;"); // Hilang/Rusak
                }
            }
        });
    }

    // ============================================================
    // INNER CLASS
    // ============================================================
    public static class CartItemDisplay {
        private final int no;
        private final String namaBarang;
        private final String pemilik;
        private final int jumlah;
        private final LocalDate tglPinjam;
        private final LocalDate tglKembali;
        private final CartItem originalItem;

        public CartItemDisplay(int no, CartItem item) {
            this.no = no;
            this.namaBarang = item.getBarang().getNamaBarang();
            this.pemilik = item.getBarang().getNamaPemilik() != null ? item.getBarang().getNamaPemilik() : "Umum";
            this.jumlah = item.getJumlahPinjam();
            this.tglPinjam = item.getTglPinjam();
            this.tglKembali = item.getTglKembali();
            this.originalItem = item;
        }
        // Getters needed for PropertyValueFactory
        public int getNo() { return no; }
        public String getNamaBarang() { return namaBarang; }
        public String getPemilik() { return pemilik; }
        public int getJumlah() { return jumlah; }
        public LocalDate getTglPinjam() { return tglPinjam; }
        public LocalDate getTglKembali() { return tglKembali; }
        public CartItem getOriginalItem() { return originalItem; }
    }
}