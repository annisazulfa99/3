-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: inventaris_kampus
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `barang`
--

DROP TABLE IF EXISTS `barang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `barang` (
  `id_barang` int NOT NULL AUTO_INCREMENT,
  `nama_barang` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `id_instansi` int NOT NULL,
  `jumlah_total` int NOT NULL,
  `jumlah_tersedia` int DEFAULT NULL,
  `foto_barang` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_barang`),
  UNIQUE KEY `id_barang_UNIQUE` (`id_barang`),
  KEY `idx_barang_id_instansi` (`id_instansi`),
  CONSTRAINT `fk_barang_id_instansi` FOREIGN KEY (`id_instansi`) REFERENCES `user` (`id_user`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `barang`
--

LOCK TABLES `barang` WRITE;
/*!40000 ALTER TABLE `barang` DISABLE KEYS */;
/*!40000 ALTER TABLE `barang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `berita`
--

DROP TABLE IF EXISTS `berita`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `berita` (
  `id_berita` int NOT NULL AUTO_INCREMENT,
  `judul` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `deskripsi` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `warna_background` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '#D9696F',
  `created_by` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_berita`),
  KEY `idx_created_at` (`created_at` DESC),
  KEY `idx_created_by` (`created_by`),
  CONSTRAINT `berita_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`id_user`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `berita`
--

LOCK TABLES `berita` WRITE;
/*!40000 ALTER TABLE `berita` DISABLE KEYS */;
/*!40000 ALTER TABLE `berita` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `borrow`
--

DROP TABLE IF EXISTS `borrow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `borrow` (
  `id_borrow` int NOT NULL AUTO_INCREMENT,
  `id_peminjam` int NOT NULL,
  `id_barang` int NOT NULL,
  `jumlah_pinjam` int NOT NULL,
  `tgl_pinjam` datetime NOT NULL,
  `dl_pengembalian` datetime NOT NULL,
  `status` enum('menunggu','disetujui','ditolak') NOT NULL DEFAULT 'menunggu',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_borrow`),
  UNIQUE KEY `id_borrow_UNIQUE` (`id_borrow`),
  KEY `fk_borrow_id_peminjam` (`id_peminjam`),
  KEY `fk_borrow_id_barang_idx` (`id_barang`),
  CONSTRAINT `fk_borrow_id_barang` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_borrow_id_peminjam` FOREIGN KEY (`id_peminjam`) REFERENCES `user` (`id_user`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `borrow`
--

LOCK TABLES `borrow` WRITE;
/*!40000 ALTER TABLE `borrow` DISABLE KEYS */;
/*!40000 ALTER TABLE `borrow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `laporan`
--

DROP TABLE IF EXISTS `laporan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `laporan` (
  `id_laporan` int NOT NULL AUTO_INCREMENT,
  `id_admin` int NOT NULL,
  `id_peminjaman` int NOT NULL,
  `tgl_lapor` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('diproses','verify','selesai') NOT NULL DEFAULT 'diproses',
  `update_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_laporan`),
  UNIQUE KEY `id_laporan_UNIQUE` (`id_laporan`),
  KEY `fk_laporan_id_admin` (`id_admin`),
  KEY `fk_laporan_id_peminjaman_idx` (`id_peminjaman`),
  CONSTRAINT `fk_laporan_id_admin` FOREIGN KEY (`id_admin`) REFERENCES `user` (`id_user`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_laporan_id_peminjaman` FOREIGN KEY (`id_peminjaman`) REFERENCES `borrow` (`id_borrow`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `laporan`
--

LOCK TABLES `laporan` WRITE;
/*!40000 ALTER TABLE `laporan` DISABLE KEYS */;
/*!40000 ALTER TABLE `laporan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return`
--

DROP TABLE IF EXISTS `return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return` (
  `id_return` int NOT NULL,
  `id_borrow` int NOT NULL,
  `jumlah` int NOT NULL,
  `kondisi` enum('baik','rusak','hilang') NOT NULL,
  `created_at` timestamp NOT NULL,
  PRIMARY KEY (`id_return`),
  UNIQUE KEY `id_return_UNIQUE` (`id_return`),
  KEY `idx_return_id_borrow` (`id_borrow`) /*!80000 INVISIBLE */,
  CONSTRAINT `fk_return_id_borrow` FOREIGN KEY (`id_borrow`) REFERENCES `borrow` (`id_borrow`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return`
--

LOCK TABLES `return` WRITE;
/*!40000 ALTER TABLE `return` DISABLE KEYS */;
/*!40000 ALTER TABLE `return` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id_user` int NOT NULL AUTO_INCREMENT,
  `username` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `password` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL,
  `nama` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` enum('peminjam','instansi','admin') NOT NULL,
  `status` enum('aktif','nonaktif') NOT NULL DEFAULT 'aktif',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `id_user_UNIQUE` (`id_user`),
  KEY `idx_user_role` (`role`),
  KEY `idx_user_status` (`status`),
  KEY `idx_user_created_at` (`created_at`),
  KEY `idx_user_update_at` (`update_at`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin_011','PassAd123','Admin Upi PWK','admin','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(2,'Mekatronik21','2120HIMA','HIMATRONIKA-AI','instansi','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(3,'567Bempwk','45AWRema','BEM REMA UPI PWK','instansi','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(4,'1256Leppim','Leppim1920','LEPPIM UPI PWK','instansi','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(5,'2403117','0683208','Kholistiyani Zulfi','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(6,'2403116','0683AW8','Nafila Ajmal','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(7,'2503117','0FG3208','Annisa Zulfa','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(8,'2203217','068320P','Reifana Al-Kindi','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(9,'2203117','06GF208','Yasir Ahmadin','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(10,'2303117','0783208','Arya Adimanggala','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(11,'2403457','0683248','Dita Karang','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(12,'2503111','0HI8208','Muhammad Zayyan','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(13,'2243117','0676890','Mark Lee','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(14,'2583117','0623HJ8','Lee Seunghyun','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(15,'2456789','WJ89008','Hwang Renjun','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(16,'2403167','JK83208','Dilraba Dilmurat','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(17,'2356781','06KL008','Martin Edwards','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(18,'2403223','HJ90208','Zhou Yufan James','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(19,'2402113','GH67208','Dasha Taran','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(20,'2567890','KL90208','Pharita Boonpakdeethaveeyod','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47'),(21,'2478907','56FG208','Roseanne Park','peminjam','aktif','2025-12-15 15:18:47','2025-12-15 15:18:47');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-23 20:33:52
