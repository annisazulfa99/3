// ================================================================
// File: src/main/java/com/inventaris/dao/BorrowDAO.java
// ================================================================
package com.inventaris.dao;

import com.inventaris.config.DatabaseConfig;
import com.inventaris.model.Borrow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * BorrowDAO - Data Access Object for Borrow
 * Handles all database operations related to Borrow entity
 */
public class BorrowDAO {
    
    private final DatabaseConfig dbConfig;
    
    /**
     * Constructor
     */
    public BorrowDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Create peminjaman baru
     * 
     * @param borrow Borrow object
     * @return true if successful, false otherwise
     */
    public boolean create(Borrow borrow) {
        String sql = "INSERT INTO borrow (id_peminjam, kode_barang, jumlah_pinjam, tgl_peminjaman, " +
                     "tgl_pinjam, dl_kembali, status_barang) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Insert borrow
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, borrow.getIdPeminjam());
            stmt.setString(2, borrow.getKodeBarang());
            stmt.setInt(3, borrow.getJumlahPinjam());
            stmt.setDate(4, Date.valueOf(borrow.getTglPeminjaman()));
            stmt.setDate(5, Date.valueOf(borrow.getTglPinjam()));
            stmt.setDate(6, Date.valueOf(borrow.getDlKembali()));
            stmt.setString(7, borrow.getStatusBarang());
            
            boolean inserted = stmt.executeUpdate() > 0;
            
            if (inserted) {
                // Update jumlah tersedia barang
                String sqlUpdate = "UPDATE barang SET jumlah_tersedia = jumlah_tersedia - ? WHERE kode_barang = ?";
                PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
                stmtUpdate.setInt(1, borrow.getJumlahPinjam());
                stmtUpdate.setString(2, borrow.getKodeBarang());
                stmtUpdate.executeUpdate();
                stmtUpdate.close();
                
                conn.commit();
                stmt.close();
                return true;
            }
            
            conn.rollback();
            stmt.close();
            return false;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error creating borrow: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Get borrow by ID
     * 
     * @param id ID peminjaman
     * @return Borrow object if found, null otherwise
     */
    public Borrow getById(int id) {
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "WHERE b.id_peminjaman = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractBorrowFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting borrow: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all peminjaman
     * 
     * @return List of all borrow
     */
    public List<Borrow> getAll() {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "ORDER BY b.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all borrow: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get peminjaman by peminjam ID
     * 
     * @param peminjamId ID peminjam
     * @return List of borrow by peminjam
     */
    public List<Borrow> getByPeminjamId(int peminjamId) {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "WHERE b.id_peminjam = ? " +
                     "ORDER BY b.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, peminjamId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting borrow by peminjam: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get active peminjaman (dipinjam)
     * 
     * @return List of active borrow
     */
    public List<Borrow> getActiveBorrows() {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "WHERE b.status_barang = 'dipinjam' " +
                     "ORDER BY b.dl_kembali ASC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting active borrows: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get pending peminjaman
     * 
     * @return List of pending borrow
     */
    public List<Borrow> getPendingBorrows() {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "WHERE b.status_barang = 'pending' " +
                     "ORDER BY b.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pending borrows: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Approve peminjaman (admin)
     * 
     * @param borrowId ID peminjaman
     * @param adminId ID admin yang approve
     * @return true if successful, false otherwise
     */
    public boolean approve(int borrowId, int adminId) {
        String sql = "UPDATE borrow SET status_barang = 'dipinjam', id_admin = ? WHERE id_peminjaman = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adminId);
            stmt.setInt(2, borrowId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error approving borrow: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Reject peminjaman (admin)
     * 
     * @param borrowId ID peminjaman
     * @return true if successful, false otherwise
     */
    public boolean reject(int borrowId) {
        Connection conn = null;
        
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Get borrow details
            Borrow borrow = getById(borrowId);
            if (borrow == null) {
                conn.rollback();
                return false;
            }
            
            // Kembalikan stok
            String sqlUpdate = "UPDATE barang SET jumlah_tersedia = jumlah_tersedia + ? WHERE kode_barang = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setInt(1, borrow.getJumlahPinjam());
            stmtUpdate.setString(2, borrow.getKodeBarang());
            stmtUpdate.executeUpdate();
            stmtUpdate.close();
            
            // Delete borrow
            String sqlDelete = "DELETE FROM borrow WHERE id_peminjaman = ?";
            PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete);
            stmtDelete.setInt(1, borrowId);
            boolean deleted = stmtDelete.executeUpdate() > 0;
            stmtDelete.close();
            
            if (deleted) {
                conn.commit();
                return true;
            }
            
            conn.rollback();
            return false;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error rejecting borrow: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Kembalikan barang
     * 
     * @param borrowId ID peminjaman
     * @param kondisi Kondisi barang saat dikembalikan
     * @param foto Foto pengembalian (optional)
     * @return true if successful, false otherwise
     */
    public boolean returnItem(int borrowId, String kondisi, String foto) {
        Connection conn = null;
        
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Get borrow details
            Borrow borrow = getById(borrowId);
            if (borrow == null) {
                conn.rollback();
                return false;
            }
            
            // Update borrow
            String sqlUpdate = "UPDATE borrow SET status_barang = 'dikembalikan', tgl_kembali = ?, " +
                              "kondisi_barang = ?, foto_pengembalian = ? WHERE id_peminjaman = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setDate(1, Date.valueOf(LocalDate.now()));
            stmtUpdate.setString(2, kondisi);
            stmtUpdate.setString(3, foto);
            stmtUpdate.setInt(4, borrowId);
            stmtUpdate.executeUpdate();
            stmtUpdate.close();
            
            // Kembalikan stok
            String sqlBarang = "UPDATE barang SET jumlah_tersedia = jumlah_tersedia + ? WHERE kode_barang = ?";
            PreparedStatement stmtBarang = conn.prepareStatement(sqlBarang);
            stmtBarang.setInt(1, borrow.getJumlahPinjam());
            stmtBarang.setString(2, borrow.getKodeBarang());
            stmtBarang.executeUpdate();
            stmtBarang.close();
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error returning item: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Get overdue borrows
     * 
     * @return List of overdue borrow
     */
    public List<Borrow> getOverdueBorrows() {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "WHERE b.status_barang = 'dipinjam' AND b.dl_kembali < CURDATE() " +
                     "ORDER BY b.dl_kembali ASC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting overdue borrows: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Extract Borrow object from ResultSet
     * 
     * @param rs ResultSet
     * @return Borrow object
     * @throws SQLException if error occurs
     */
    private Borrow extractBorrowFromResultSet(ResultSet rs) throws SQLException {
        Borrow borrow = new Borrow();
        borrow.setIdPeminjaman(rs.getInt("id_peminjaman"));
        borrow.setIdPeminjam(rs.getInt("id_peminjam"));
        
        int adminId = rs.getInt("id_admin");
        borrow.setIdAdmin(rs.wasNull() ? null : adminId);
        
        borrow.setKodeBarang(rs.getString("kode_barang"));
        borrow.setJumlahPinjam(rs.getInt("jumlah_pinjam"));
        borrow.setKondisiBarang(rs.getString("kondisi_barang"));
        borrow.setTglPeminjaman(rs.getDate("tgl_peminjaman").toLocalDate());
        borrow.setTglPinjam(rs.getDate("tgl_pinjam").toLocalDate());
        
        Date tglKembali = rs.getDate("tgl_kembali");
        borrow.setTglKembali(tglKembali != null ? tglKembali.toLocalDate() : null);
        
        borrow.setDlKembali(rs.getDate("dl_kembali").toLocalDate());
        borrow.setFotoPengembalian(rs.getString("foto_pengembalian"));
        borrow.setStatusBarang(rs.getString("status_barang"));
        borrow.setCreatedAt(rs.getTimestamp("created_at"));
        
        // Extended properties
        borrow.setNamaPeminjam(rs.getString("nama_peminjam"));
        borrow.setNoTelepon(rs.getString("no_telepon"));
        borrow.setNamaBarang(rs.getString("nama_barang"));
        
        return borrow;
    }
}