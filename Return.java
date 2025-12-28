// ================================================================
// File: src/main/java/com/inventaris/model/Return.java
// ================================================================
package com.inventaris.model;

import java.sql.Timestamp;

/**
 * Return Model Class
 * Represents item return transaction
 */
public class Return {
    
    private int idReturn;
    private int idBorrow;
    private int jumlah;
    private String kondisi; // baik, rusak, hilang
    private Timestamp createdAt;
    private String status; // pending, verified, rejected
    private String keterangan;
    
    // Extended properties (from JOIN)
    private String namaBarang;
    private String namaPeminjam;
    private String kodeBarang;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    public Return() {}
    
    public Return(int idBorrow, int jumlah, String kondisi, String keterangan) {
        this.idBorrow = idBorrow;
        this.jumlah = jumlah;
        this.kondisi = kondisi;
        this.keterangan = keterangan;
        this.status = "pending";
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdReturn() {
        return idReturn;
    }
    
    public void setIdReturn(int idReturn) {
        this.idReturn = idReturn;
    }
    
    public int getIdBorrow() {
        return idBorrow;
    }
    
    public void setIdBorrow(int idBorrow) {
        this.idBorrow = idBorrow;
    }
    
    public int getJumlah() {
        return jumlah;
    }
    
    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }
    
    public String getKondisi() {
        return kondisi;
    }
    
    public void setKondisi(String kondisi) {
        this.kondisi = kondisi;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getKeterangan() {
        return keterangan;
    }
    
    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    
    // Extended properties
    
    public String getNamaBarang() {
        return namaBarang;
    }
    
    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }
    
    public String getNamaPeminjam() {
        return namaPeminjam;
    }
    
    public void setNamaPeminjam(String namaPeminjam) {
        this.namaPeminjam = namaPeminjam;
    }
    
    public String getKodeBarang() {
        return kodeBarang;
    }
    
    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }
    
    public boolean isVerified() {
        return "verified".equalsIgnoreCase(status);
    }
    
    public boolean isRejected() {
        return "rejected".equalsIgnoreCase(status);
    }
    
    public boolean isGoodCondition() {
        return "baik".equalsIgnoreCase(kondisi);
    }
    
    @Override
    public String toString() {
        return "Return{" +
                "idReturn=" + idReturn +
                ", idBorrow=" + idBorrow +
                ", jumlah=" + jumlah +
                ", kondisi='" + kondisi + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}