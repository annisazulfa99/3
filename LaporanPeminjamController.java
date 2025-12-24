package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.dao.BarangDAO;
import com.inventaris.dao.BorrowDAO;
import com.inventaris.dao.LaporDAO;
import com.inventaris.model.Borrow;
import com.inventaris.model.Lapor;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class LaporanPeminjamController implements Initializable {

    // --- UI COMPONENTS ---
    @FXML private BorderPane rootPane;
    @FXML private TextField txtSearch;
    
    // Buttons Navigation
  

    // View Containers
    @FXML private VBox viewPeminjam; // Form Coklat
    @FXML private VBox viewAdmin;    // Tabel Admin

    // Form Components (Peminjam)
    @FXML private ComboBox<Borrow> peminjamanCombo;
    @FXML private TextField txtInstansi; // Field baru untuk Instansi
    @FXML private TextArea keteranganArea;
    @FXML private Button btnLapor;

    // Table Components (Admin)
    @FXML private TableView<Lapor> laporTable;
    @FXML private TableColumn<Lapor, String> colNoLaporan, colPeminjam, colBarang, colStatus;
    @FXML private TableColumn<Lapor, LocalDate> colTglLaporan;
    @FXML private TableColumn<Lapor, Void> colAction;

    // --- DATA TOOLS ---
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final LaporDAO laporDAO = new LaporDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private final BarangDAO barangDAO = new BarangDAO();
   @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Atur Menu Navigasi sesuai Role
        String role = sessionManager.getCurrentRole();
   

        // 2. Logika Tampilan Berdasarkan Role
        if ("admin".equals(role)) {
            

        } else {
            // --- TAMPILAN PEMINJAM / INSTANSI ---
            if (viewAdmin != null) {
                viewAdmin.setVisible(false);
                viewAdmin.setManaged(false);
            }
            if (viewPeminjam != null) {
                viewPeminjam.setVisible(true);
                viewPeminjam.setManaged(true);
            }

            // Load data barang yang sedang dipinjam user ke ComboBox
            loadUserBorrows();

            // 3. LOGIKA OTOMATIS: Isi Nama Instansi saat Barang Dipilih
            peminjamanCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    // A. Ambil kode barang dari item yang dipilih di ComboBox
                    String kodeBarang = newVal.getKodeBarang();

                    // B. Cari detail barang ke Database pakai BarangDAO
                    // (Kita lakukan ini karena di tabel Borrow tidak ada nama pemilik)
                    com.inventaris.model.Barang barangDetail = barangDAO.getByKode(kodeBarang);

                    // C. Tampilkan Nama Pemilik di TextField Instansi
                    if (barangDetail != null) {
                        String pemilik = barangDetail.getNamaPemilik();
                        txtInstansi.setText(pemilik != null ? pemilik : "Umum");
                    } else {
                        txtInstansi.setText("Tidak diketahui");
                    }
                } else {
                    // D. Jika tidak ada yang dipilih (kosongkan field)
                    txtInstansi.setText("-");
                }
            });
        }
        
        System.out.println("✅ Laporan Controller initialized for role: " + role);
    }

    // --- LOGIKA FORM PEMINJAM ---

    private void loadUserBorrows() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) return;

            List<Borrow> borrows = borrowDAO.getByPeminjamId(peminjamId);
            // Hanya tampilkan barang yang statusnya sedang dipinjam
            borrows.removeIf(b -> !"dipinjam".equals(b.getStatusBarang()));

            ObservableList<Borrow> list = FXCollections.observableArrayList(borrows);
            peminjamanCombo.setItems(list);

            // Tampilkan nama barang yang user-friendly di ComboBox
            peminjamanCombo.setButtonCell(new BorrowListCell());
            peminjamanCombo.setCellFactory(lv -> new BorrowListCell());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLapor() {
        if (peminjamanCombo.getValue() == null) {
            AlertUtil.showWarning("Validasi", "Harap pilih barang yang bermasalah!");
            return;
        }
        if (keteranganArea.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validasi", "Mohon isi keterangan laporan.");
            return;
        }

        if (!AlertUtil.showConfirmation("Kirim Laporan", "Apakah Anda yakin ingin mengajukan laporan ini?")) {
            return;
        }
try {
            Borrow borrow = peminjamanCombo.getValue();
            String noLaporan = laporDAO.generateNoLaporan();

            Lapor lapor = new Lapor();
            lapor.setNoLaporan(noLaporan);
            lapor.setIdPeminjaman(borrow.getIdPeminjaman());
            lapor.setKodeBarang(borrow.getKodeBarang());
            lapor.setStatus("diproses");
            lapor.setTglLaporan(LocalDate.now());
            
            // --- [TAMBAHAN WAJIB DI SINI] ---
            // Ambil teks dari inputan user, simpan ke objek
            lapor.setKeterangan(keteranganArea.getText()); 
            // --------------------------------

            if (laporDAO.create(lapor)) {
                AlertUtil.showSuccess("Sukses", "Laporan terkirim! Admin akan segera memproses.");
                LogActivityUtil.logCreate(sessionManager.getCurrentUsername(), sessionManager.getCurrentRole(), "Laporan", noLaporan);
                
                // Reset Form
                peminjamanCombo.setValue(null);
                txtInstansi.setText("-");
                keteranganArea.clear();
            }  else {
                AlertUtil.showError("Gagal", "Gagal mengirim laporan.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       
           
    }

 
    
    // Class helper untuk menampilkan nama barang di ComboBox
    private static class BorrowListCell extends ListCell<Borrow> {
        @Override
        protected void updateItem(Borrow item, boolean empty) {
            super.updateItem(item, empty);
            setText((empty || item == null) ? null : item.getNamaBarang());
        }
    }
}