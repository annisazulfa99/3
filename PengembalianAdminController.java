// ================================================================
// File: src/main/java/com/inventaris/controller/PengembalianAdminController.java
// ================================================================
package com.inventaris.controller;

import com.inventaris.dao.ReturnDAO;
import com.inventaris.model.Return;
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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk Admin - Verifikasi Pengembalian
 */
public class PengembalianAdminController implements Initializable {
    
    // Statistics
    @FXML private Label lblPending;
    @FXML private Label lblVerified;
    @FXML private Label lblRejected;
    
    // Table
    @FXML private TableView<Return> tableReturn;
    @FXML private TableColumn<Return, Integer> colReturnId;
    @FXML private TableColumn<Return, String> colPeminjam;
    @FXML private TableColumn<Return, String> colBarang;
    @FXML private TableColumn<Return, Integer> colJumlah;
    @FXML private TableColumn<Return, String> colKondisi;
    @FXML private TableColumn<Return, String> colKeterangan;
    @FXML private TableColumn<Return, String> colTanggal;
    @FXML private TableColumn<Return, String> colStatus;
    @FXML private TableColumn<Return, Void> colAction;
    
    @FXML private ComboBox<String> filterStatusCombo;
    
    private final ReturnDAO returnDAO = new ReturnDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check admin access
        if (!sessionManager.isAdmin()) {
            AlertUtil.showError("Access Denied", "Hanya admin yang dapat mengakses halaman ini!");
            return;
        }
        
        setupTable();
        setupFilter();
        loadData();
        updateStatistics();
        
        System.out.println("✅ Pengembalian Admin Controller initialized");
    }
    
    // ============================================================
    // TABLE SETUP
    // ============================================================
    
    private void setupTable() {
        colReturnId.setCellValueFactory(new PropertyValueFactory<>("idReturn"));
        colPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colKondisi.setCellValueFactory(new PropertyValueFactory<>("kondisi"));
        colKeterangan.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        
        colTanggal.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null ? 
                cellData.getValue().getCreatedAt().toString().substring(0, 16) : "-"
            )
        );
        
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Kondisi dengan warna
        colKondisi.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String kondisi, boolean empty) {
                super.updateItem(kondisi, empty);
                if (empty || kondisi == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(kondisi.toUpperCase());
                    switch (kondisi.toLowerCase()) {
                        case "baik":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                            break;
                        case "rusak":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;");
                            break;
                        case "hilang":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        // Status dengan warna
        colStatus.setCellFactory(col -> new TableCell<>() {
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
        
        // Action buttons
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnVerify = new Button("✅ Verifikasi");
            private final Button btnReject = new Button("❌ Tolak");
            private final Button btnDetail = new Button("👁️ Detail");
            private final HBox buttons = new HBox(5, btnDetail, btnVerify, btnReject);
            
            {
                btnVerify.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                                  "-fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnReject.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                                  "-fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnDetail.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; " +
                                  "-fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                
                btnVerify.setOnAction(e -> {
                    Return returnItem = getTableView().getItems().get(getIndex());
                    handleVerify(returnItem);
                });
                
                btnReject.setOnAction(e -> {
                    Return returnItem = getTableView().getItems().get(getIndex());
                    handleReject(returnItem);
                });
                
                btnDetail.setOnAction(e -> {
                    Return returnItem = getTableView().getItems().get(getIndex());
                    showDetail(returnItem);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Return returnItem = getTableView().getItems().get(getIndex());
                    
                    if ("pending".equals(returnItem.getStatus())) {
                        // Pending: Show all buttons
                        setGraphic(buttons);
                    } else {
                        // Already processed: Only detail button
                        HBox detailOnly = new HBox(btnDetail);
                        setGraphic(detailOnly);
                    }
                }
            }
        });
    }
    
    private void setupFilter() {
        filterStatusCombo.setItems(FXCollections.observableArrayList(
            "Semua Status", "Pending", "Verified", "Rejected"
        ));
        filterStatusCombo.setValue("Semua Status");
        filterStatusCombo.setOnAction(e -> loadData());
    }
    
    // ============================================================
    // DATA LOADING
    // ============================================================
    
    private void loadData() {
        try {
            List<Return> returns = returnDAO.getAll();
            
            // Filter by status
            String filter = filterStatusCombo.getValue();
            if (filter != null && !"Semua Status".equals(filter)) {
                returns.removeIf(r -> !r.getStatus().equalsIgnoreCase(filter));
            }
            
            tableReturn.setItems(FXCollections.observableArrayList(returns));
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data pengembalian!");
            e.printStackTrace();
        }
    }
    
    private void updateStatistics() {
        try {
            List<Return> all = returnDAO.getAll();
            
            long pending = all.stream().filter(Return::isPending).count();
            long verified = all.stream().filter(Return::isVerified).count();
            long rejected = all.stream().filter(Return::isRejected).count();
            
            lblPending.setText(String.valueOf(pending));
            lblVerified.setText(String.valueOf(verified));
            lblRejected.setText(String.valueOf(rejected));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ============================================================
    // ACTIONS
    // ============================================================
    
    private void handleVerify(Return returnItem) {
        if (!AlertUtil.showConfirmation("Konfirmasi Verifikasi", 
            "Verifikasi pengembalian ini?\n\n" +
            "Peminjam: " + returnItem.getNamaPeminjam() + "\n" +
            "Barang: " + returnItem.getNamaBarang() + "\n" +
            "Jumlah: " + returnItem.getJumlah() + "\n" +
            "Kondisi: " + returnItem.getKondisi())) {
            return;
        }
        
        if (returnDAO.verify(returnItem.getIdReturn())) {
            AlertUtil.showSuccess("Berhasil", "Pengembalian berhasil diverifikasi!\nStok barang telah dikembalikan.");
            
            LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                "Verifikasi pengembalian: " + returnItem.getNamaBarang() + 
                " dari " + returnItem.getNamaPeminjam(),
                "VERIFY_RETURN",
                sessionManager.getCurrentRole()
            );
            
            loadData();
            updateStatistics();
        } else {
            AlertUtil.showError("Gagal", "Gagal memverifikasi pengembalian!");
        }
    }
    
    private void handleReject(Return returnItem) {
        if (!AlertUtil.showConfirmation("Konfirmasi Penolakan", 
            "Tolak pengembalian ini?\n\n" +
            "Peminjam: " + returnItem.getNamaPeminjam() + "\n" +
            "Barang: " + returnItem.getNamaBarang())) {
            return;
        }
        
        if (returnDAO.reject(returnItem.getIdReturn())) {
            AlertUtil.showSuccess("Berhasil", "Pengembalian ditolak!");
            
            LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                "Tolak pengembalian: " + returnItem.getNamaBarang() + 
                " dari " + returnItem.getNamaPeminjam(),
                "REJECT_RETURN",
                sessionManager.getCurrentRole()
            );
            
            loadData();
            updateStatistics();
        } else {
            AlertUtil.showError("Gagal", "Gagal menolak pengembalian!");
        }
    }
    
    private void showDetail(Return returnItem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail Pengembalian");
        alert.setHeaderText("ID Return: " + returnItem.getIdReturn());
        
        String content = String.format(
            "Peminjam: %s\n" +
            "Barang: %s\n" +
            "Jumlah: %d\n" +
            "Kondisi: %s\n" +
            "Status: %s\n" +
            "Tanggal: %s\n\n" +
            "Keterangan:\n%s",
            returnItem.getNamaPeminjam(),
            returnItem.getNamaBarang(),
            returnItem.getJumlah(),
            returnItem.getKondisi(),
            returnItem.getStatus(),
            returnItem.getCreatedAt() != null ? returnItem.getCreatedAt().toString().substring(0, 16) : "-",
            returnItem.getKeterangan()
        );
        
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
        updateStatistics();
        AlertUtil.showInfo("Refresh", "Data berhasil diperbarui!");
    }
}