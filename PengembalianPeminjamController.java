// ================================================================
// File: src/main/java/com/inventaris/controller/PengembalianPeminjamController.java
// ================================================================
package com.inventaris.controller;

import com.inventaris.dao.BorrowDAO;
import com.inventaris.dao.ReturnDAO;
import com.inventaris.model.Borrow;
import com.inventaris.model.Return;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk Peminjam - Ajukan Pengembalian
 */
public class PengembalianPeminjamController implements Initializable {
    
    // TABLE: Barang yang Dipinjam
    @FXML private TableView<Borrow> tableDipinjam;
    @FXML private TableColumn<Borrow, Integer> colId;
    @FXML private TableColumn<Borrow, String> colNama;
    @FXML private TableColumn<Borrow, Integer> colJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colDeadline;
    @FXML private TableColumn<Borrow, Long> colSisaHari;
    @FXML private TableColumn<Borrow, Void> colAction;
    
    // TABLE: Riwayat Pengembalian
    @FXML private TableView<Return> tableRiwayat;
    @FXML private TableColumn<Return, Integer> colRiwayatId;
    @FXML private TableColumn<Return, String> colRiwayatNama;
    @FXML private TableColumn<Return, Integer> colRiwayatJumlah;
    @FXML private TableColumn<Return, String> colRiwayatKondisi;
    @FXML private TableColumn<Return, String> colRiwayatTgl;
    @FXML private TableColumn<Return, String> colRiwayatStatus;
    
    @FXML private ComboBox<String> filterStatus;
    
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final ReturnDAO returnDAO = new ReturnDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableDipinjam();
        setupTableRiwayat();
        setupFilter();
        
        loadDipinjam();
        loadRiwayat();
        
        System.out.println("✅ Pengembalian Peminjam Controller initialized");
    }
    
    // ============================================================
    // TABLE SETUP
    // ============================================================
    
    private void setupTableDipinjam() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPeminjaman"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("dlKembali"));
        
        // Sisa Hari dengan warna
        colSisaHari.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSisaHari())
        );
        
        colSisaHari.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long sisaHari, boolean empty) {
                super.updateItem(sisaHari, empty);
                if (empty || sisaHari == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(sisaHari + " hari");
                    if (sisaHari < 0) {
                        setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #742a2a; -fx-font-weight: bold;");
                    } else if (sisaHari <= 2) {
                        setStyle("-fx-background-color: #feebc8; -fx-text-fill: #7c2d12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #c6f6d5; -fx-text-fill: #22543d;");
                    }
                }
            }
        });
        
        // Tombol Kembalikan
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnReturn = new Button("📦 Kembalikan");
            
            {
                btnReturn.setStyle("-fx-background-color: #6A5436; -fx-text-fill: white; " +
                                  "-fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnReturn.setOnAction(e -> {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    handleReturn(borrow);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    // Cek apakah sudah ada return request pending
                    if (returnDAO.hasReturnRequest(borrow.getIdPeminjaman())) {
                        Button btnPending = new Button("⏳ Pending");
                        btnPending.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; " +
                                          "-fx-font-weight: bold; -fx-background-radius: 5;");
                        btnPending.setDisable(true);
                        setGraphic(btnPending);
                    } else {
                        setGraphic(btnReturn);
                    }
                }
            }
        });
    }
    
    private void setupTableRiwayat() {
        colRiwayatId.setCellValueFactory(new PropertyValueFactory<>("idReturn"));
        colRiwayatNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colRiwayatJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colRiwayatKondisi.setCellValueFactory(new PropertyValueFactory<>("kondisi"));
        
        colRiwayatTgl.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null ? 
                cellData.getValue().getCreatedAt().toString().substring(0, 16) : "-"
            )
        );
        
        colRiwayatStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Status dengan warna
        colRiwayatStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toUpperCase());
                    switch (status.toLowerCase()) {
                        case "pending":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;");
                            break;
                        case "verified":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                            break;
                        case "rejected":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }
    
    private void setupFilter() {
        filterStatus.setItems(FXCollections.observableArrayList(
            "Semua Status", "Pending", "Verified", "Rejected"
        ));
        filterStatus.setValue("Semua Status");
        filterStatus.setOnAction(e -> loadRiwayat());
    }
    
    // ============================================================
    // DATA LOADING
    // ============================================================
    
    private void loadDipinjam() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) return;
            
            List<Borrow> borrows = borrowDAO.getByPeminjamId(peminjamId);
            borrows.removeIf(b -> !"dipinjam".equals(b.getStatusBarang()));
            
            tableDipinjam.setItems(FXCollections.observableArrayList(borrows));
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data peminjaman!");
            e.printStackTrace();
        }
    }
    
    private void loadRiwayat() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) return;
            
            List<Return> returns = returnDAO.getByPeminjam(peminjamId);
            
            // Filter by status
            String filter = filterStatus.getValue();
            if (filter != null && !"Semua Status".equals(filter)) {
                returns.removeIf(r -> !r.getStatus().equalsIgnoreCase(filter));
            }
            
            tableRiwayat.setItems(FXCollections.observableArrayList(returns));
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat riwayat pengembalian!");
            e.printStackTrace();
        }
    }
    
    // ============================================================
    // ACTIONS
    // ============================================================
    
    private void handleReturn(Borrow borrow) {
        // Dialog Form Pengembalian
        Dialog<Return> dialog = new Dialog<>();
        dialog.setTitle("Ajukan Pengembalian");
        dialog.setHeaderText("Pengembalian: " + borrow.getNamaBarang());
        
        ButtonType btnSubmit = new ButtonType("Ajukan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Label lblJumlah = new Label("Jumlah yang dikembalikan:");
        Spinner<Integer> spinJumlah = new Spinner<>(1, borrow.getJumlahPinjam(), borrow.getJumlahPinjam());
        spinJumlah.setEditable(true);
        
        Label lblKondisi = new Label("Kondisi Barang:");
        ComboBox<String> cmbKondisi = new ComboBox<>();
        cmbKondisi.getItems().addAll("baik", "rusak", "hilang");
        cmbKondisi.setValue("baik");
        
        Label lblKeterangan = new Label("Keterangan:");
        TextArea txtKeterangan = new TextArea();
        txtKeterangan.setPromptText("Jelaskan kondisi barang...");
        txtKeterangan.setPrefRowCount(3);
        
        grid.add(lblJumlah, 0, 0);
        grid.add(spinJumlah, 1, 0);
        grid.add(lblKondisi, 0, 1);
        grid.add(cmbKondisi, 1, 1);
        grid.add(lblKeterangan, 0, 2);
        grid.add(txtKeterangan, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSubmit) {
                if (txtKeterangan.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validasi", "Keterangan tidak boleh kosong!");
                    return null;
                }
                
                Return returnItem = new Return(
                    borrow.getIdPeminjaman(),
                    spinJumlah.getValue(),
                    cmbKondisi.getValue(),
                    txtKeterangan.getText().trim()
                );
                
                return returnItem;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(returnItem -> {
            if (returnDAO.create(returnItem)) {
                AlertUtil.showSuccess("Berhasil", 
                    "Pengembalian berhasil diajukan!\nMenunggu verifikasi admin.");
                
                LogActivityUtil.logCreate(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "return",
                    borrow.getNamaBarang()
                );
                
                loadDipinjam();
                loadRiwayat();
            } else {
                AlertUtil.showError("Gagal", "Gagal mengajukan pengembalian!");
            }
        });
    }
}