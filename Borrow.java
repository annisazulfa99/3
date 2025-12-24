// ================================================================
// File: src/main/java/com/inventaris/model/Borrow.java
// ================================================================
package com.inventaris.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Borrow Model Class
 * Represents a borrowing transaction in the system
 */
public class Borrow {
    
    // Attributes
    private int idPeminjaman;
    private Integer idPeminjam;
    private Integer idAdmin;
    private String kodeBarang;
    private int jumlahPinjam;
    private String kondisiBarang;
    private LocalDate tglPeminjaman;
    private LocalDate tglPinjam;
    private LocalDate tglKembali;
    private LocalDate dlKembali;
    private String fotoPengembalian;
    private String statusBarang; // dipinjam, dikembalikan, hilang, rusak, pending
    private Timestamp createdAt;
    
    // Extended properties (from JOIN queries)
    private String namaPeminjam;
    private String namaBarang;
    private String noTelepon;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    /**
     * Default constructor
     */
    public Borrow() {
        // Empty constructor
    }
    
    /**
     * Constructor with basic fields
     */
    public Borrow(int idPeminjam, String kodeBarang, int jumlahPinjam, 
                  LocalDate tglPinjam, LocalDate dlKembali) {
        this.idPeminjam = idPeminjam;
        this.kodeBarang = kodeBarang;
        this.jumlahPinjam = jumlahPinjam;
        this.tglPeminjaman = LocalDate.now();
        this.tglPinjam = tglPinjam;
        this.dlKembali = dlKembali;
        this.statusBarang = "pending";
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdPeminjaman() {
        return idPeminjaman;
    }
    
    public void setIdPeminjaman(int idPeminjaman) {
        this.idPeminjaman = idPeminjaman;
    }
    

    public Integer getIdPeminjam() {
        return idPeminjam;
    }

    public void setIdPeminjam(Integer idPeminjam) {
        this.idPeminjam = idPeminjam;
    }

    public Integer getIdAdmin() {
        return idAdmin;
    }
    
    public void setIdAdmin(Integer idAdmin) {
        this.idAdmin = idAdmin;
    }
    
    public String getKodeBarang() {
        return kodeBarang;
    }
    
    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }
    
    public int getJumlahPinjam() {
        return jumlahPinjam;
    }
    
    public void setJumlahPinjam(int jumlahPinjam) {
        this.jumlahPinjam = jumlahPinjam;
    }
    
    public String getKondisiBarang() {
        return kondisiBarang;
    }
    
    public void setKondisiBarang(String kondisiBarang) {
        this.kondisiBarang = kondisiBarang;
    }
    
    public LocalDate getTglPeminjaman() {
        return tglPeminjaman;
    }
    
    public void setTglPeminjaman(LocalDate tglPeminjaman) {
        this.tglPeminjaman = tglPeminjaman;
    }
    
    public LocalDate getTglPinjam() {
        return tglPinjam;
    }
    
    public void setTglPinjam(LocalDate tglPinjam) {
        this.tglPinjam = tglPinjam;
    }
    
    public LocalDate getTglKembali() {
        return tglKembali;
    }
    
    public void setTglKembali(LocalDate tglKembali) {
        this.tglKembali = tglKembali;
    }
    
    public LocalDate getDlKembali() {
        return dlKembali;
    }
    
    public void setDlKembali(LocalDate dlKembali) {
        this.dlKembali = dlKembali;
    }
    
    public String getFotoPengembalian() {
        return fotoPengembalian;
    }
    
    public void setFotoPengembalian(String fotoPengembalian) {
        this.fotoPengembalian = fotoPengembalian;
    }
    
    public String getStatusBarang() {
        return statusBarang;
    }
    
    public void setStatusBarang(String statusBarang) {
        this.statusBarang = statusBarang;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    // Extended properties
    
    public String getNamaPeminjam() {
        return namaPeminjam;
    }
    
    public void setNamaPeminjam(String namaPeminjam) {
        this.namaPeminjam = namaPeminjam;
    }
    
    public String getNamaBarang() {
        return namaBarang;
    }
    
    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }
    
    public String getNoTelepon() {
        return noTelepon;
    }
    
    public void setNoTelepon(String noTelepon) {
        this.noTelepon = noTelepon;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Calculate remaining days until deadline
     */
    public long getSisaHari() {
        if (dlKembali != null) {
            return ChronoUnit.DAYS.between(LocalDate.now(), dlKembali);
        }
        return 0;
    }
    
    /**
     * Check if borrowing is overdue
     */
    public boolean isOverdue() {
        if (dlKembali != null && "dipinjam".equalsIgnoreCase(statusBarang)) {
            return LocalDate.now().isAfter(dlKembali);
        }
        return false;
    }
    
    /**
     * Check if borrowing is active
     */
    public boolean isActive() {
        return "dipinjam".equalsIgnoreCase(statusBarang);
    }
    
    /**
     * Check if borrowing is pending
     */
    public boolean isPending() {
        return "pending".equalsIgnoreCase(statusBarang);
    }
    
    /**
     * Check if borrowing is returned
     */
    public boolean isReturned() {
        return "dikembalikan".equalsIgnoreCase(statusBarang);
    }
    
    // ============================================================
    // OVERRIDE METHODS
    // ============================================================
    
    @Override
    public String toString() {
        return "Borrow{" +
                "idPeminjaman=" + idPeminjaman +
                ", namaPeminjam='" + namaPeminjam + '\'' +
                ", namaBarang='" + namaBarang + '\'' +
                ", statusBarang='" + statusBarang + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Borrow borrow = (Borrow) obj;
        return idPeminjaman == borrow.idPeminjaman;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idPeminjaman);
    }
}