# Module 7 - Profiling

## Performance Testing Results

### JMeter Results - Before vs After Optimization

### /all-student
**Before:**
![Before JMeter All Student](assets/before_jmeter_all-student.png)
![Before CLI All Student](assets/before_CLI_all-student.png)

**After:**
![After JMeter All Student](assets/after_jmeter_all-student.png)
![After CLI All Student](assets/after_CLI_all-student.png)

**Optimization Applied:**
- Menghilangkan **N+1 Query Problem** dimana sebelumnya terjadi
  20.000+ query ke database untuk setiap mahasiswa
- Menggunakan **JOIN FETCH** pada JPQL query untuk mengambil data StudentCourse beserta Student dan Course dalam 1 query
- Jumlah query ke database berkurang drastis 
---

### /all-student-name
**Before:**
![Before JMeter All Student Name](assets/before_jmeter_all-student-name.png)
![Before CLI All Student Name](assets/before_CLI_all-student-name.png)

**After:**
![After JMeter All Student Name](assets/after_jmeter_all-student-name.png)
![After CLI All Student Name](assets/after_CLI_all-student-name.png)

**Optimization Applied:**
- Mengganti String concatenation manual (`result += name + ", "`)
  yang membuat objek String baru di setiap iterasi
- Menggunakan `Stream` dan `Collectors.joining()` yang jauh
  lebih efisien dalam penggunaan memori
- Mengurangi overhead garbage collector akibat pembuatan
  objek String yang berulang-ulang
---

### /highest-gpa
**Before:**
![Before JMeter Highest GPA](assets/before_jmeter_highest-gpa.png)
![Before CLI Highest GPA](assets/before_CLI_highest-gpa.png)

**After:**
![After JMeter Highest GPA](assets/after_jmeter_highest-gpa.png)
![After CLI Highest GPA](assets/after_CLI_highest-gpa.png)
**Optimization Applied:**
- Menghilangkan loop manual yang mengiterasi seluruh data mahasiswa
  hanya untuk mencari nilai GPA tertinggi
- Menggunakan `findStudentWithHighestGpa()` dengan JPQL query
  `ORDER BY s.gpa DESC LIMIT 1` sehingga pencarian dilakukan
  langsung di level database
- Database hanya mengembalikan 1 record teratas, bukan
  seluruh data mahasiswa

## COMPARISON
| Endpoint | Avg Before (ms) | Avg After (ms) | Min Before | Min After | Max Before | Max After | Improvement |
|---|---|---|---|---|---|---|---|
| /all-student | 202,396 | 3,079 | 198,314 | 2,780 | 205,463 | 3,242 | 98.5% ✅ |
| /all-student-name | 9,733 | 1,397 | 8,726 | 959 | 10,179 | 1,763 | 85.6% ✅ |
| /highest-gpa | 333 | 12 | 244 | 9 | 504 | 27 | 96.4% ✅ |

## Profiling Results - Before vs After Optimization

### IntelliJ Profiler Results (CPU Time)

### /all-student
**Before:**
![Before Profiler All Student](assets/before_profiler_all-student.png)

**After:**
![After Profiler All Student](assets/after_profiler_all-student.png)

---

### /all-student-name
**Before:**
![Before Profiler All Student Name](assets/before_profiler_all-student-name.png)

**After:**
![After Profiler All Student Name](assets/after_profiler_all-student-name.png)

---

### /highest-gpa
**Before:**
![Before Profiler Highest GPA](assets/before_profiler_highest-gpa.png)

**After:**
![After Profiler Highest GPA](assets/after_profiler_highest-gpa.png)

---
## COMPARISON
| Endpoint | Method | CPU Time Before (ms) | CPU Time After (ms) | Improvement |
|---|---|---|---|---|
| /all-student | getAllStudentsWithCourses() | 8,237 | 181 | 97.8% ✅ |
| /all-student-name | joinStudentNames() | 1,111 | 315 | 71.6% ✅ |
| /highest-gpa | findStudentWithHighestGpa() | 512 | 48 | 90.6% ✅ |

---

## Summary of Performance Gains

| Endpoint         | JMeter Improvement | Profiler Improvement |
|------------------|---|---|
| /all-student     | 98.5% faster | 97.8% faster |
| /all-student-name | 85.6% faster | 71.6% faster |
| /highest-gpa     | 96.4% faster | 90.6% faster |

**All endpoints achieved more than the required 20% improvement!**

---
## Reflection

### 1. What is the difference between the approach of performance testing with JMeter and profiling with IntelliJ Profiler in the context of optimizing application performance?

JMeter bekerja dari perspektif luar (black-box): mensimulasikan
banyak pengguna yang mengakses endpoint dan mengukur waktu respons
secara keseluruhan. JMeter memberitahu apa ada masalah performa,
tapi tidak menjelaskan kenapa.

IntelliJ Profiler bekerja dari dalam (white-box): ia merekam setiap
method Java yang dieksekusi, berapa lama, dan berapa CPU yang digunakan.
Profiler memberitahu kenapa ada masalah, method mana yang menjadi
bottleneck.

Keduanya saling melengkapi: JMeter untuk deteksi masalah,
IntelliJ Profiler untuk diagnosa penyebab.

---

### 2. How does the profiling process help you in identifying and understanding the weak points in your application?

Profiling menampilkan flame graph dan method list yang menunjukkan
secara visual method mana yang paling banyak mengonsumsi CPU time.
Dalam kasus ini, profiling mengungkap N+1 Query Problem pada method
getAllStudentsWithCourses(), di mana 20.000+ query database dieksekusi
secara berulang. Tanpa profiling, masalah ini sulit ditemukan hanya
dari melihat kode secara manual.

---

### 3. Do you think IntelliJ Profiler is effective in assisting you to analyze and identify bottlenecks in your application code?

Ya, IntelliJ Profiler sangat efektif karena terintegrasi langsung
dengan IDE sehingga dari flame graph bisa langsung klik ke source code
yang bermasalah. Fitur comparison view juga memudahkan verifikasi
apakah optimasi berhasil. Kelemahannya adalah overhead profiling
itu sendiri dapat sedikit memperlambat aplikasi saat diukur.

---

### 4. What are the main challenges you face when conducting performance testing and profiling, and how do you overcome these challenges?

Tantangan 1: Data seeding yang sangat lambat (20.000 mahasiswa).
Solusi: Mengurangi jumlah data sementara saat tes tes saja,
namun tetap cukup untuk merasakan perbedaan performa.

Tantangan 2: Hasil profiling bervariasi karena JIT compiler
belum optimal pada run pertama.
Solusi: Melakukan warm-up dengan beberapa kali akses endpoint
sebelum mengambil measurement yang valid.

Tantangan 3: Memahami flame graph yang kompleks.
Solusi: Fokus pada method dengan persentase CPU tertinggi
dan abaikan framework/library method.

---

### 5. What are the main benefits you gain from using IntelliJ Profiler for profiling your application code?

1. Integrasi IDE: Langsung bisa navigasi dari profiling result
   ke source code.
2. Flame Graph visual: Mudah menemukan bottleneck secara intuitif.
3. Comparison view: Bisa bandingkan sebelum dan sesudah optimasi
   secara langsung.
4. CPU vs Total Time: Bisa membedakan masalah komputasi vs I/O.
5. Tidak perlu tools eksternal tambahan untuk Java/Spring Boot.

---

### 6. How do you handle situations where the results from profiling with IntelliJ Profiler are not entirely consistent with findings from performance testing using JMeter?

Ketidakkonsistenan bisa terjadi karena profiler menambahkan overhead
pengukuran pada aplikasi. Cara mengatasinya:
1. Prioritaskan pola, bukan angka absolut, jika profiler menunjukkan
   method A lebih lambat dari B, itu tetap valid meski angkanya berbeda.
2. Gunakan profiler untuk identifikasi masalah, JMeter untuk verifikasi
   perbaikan di kondisi lebih realistis.
3. Lakukan multiple measurement dan ambil rata-rata untuk
   mengurangi noise.

---

### 7. What strategies do you implement in optimizing application code after analyzing results from performance testing and profiling? How do you ensure the changes you make do not affect the application's functionality?
Strategi optimasi yang diterapkan:
1. Identifikasi N+1 Query Problem melalui profiling
2. Ganti multiple queries dengan single query menggunakan
   Spring Data JPA method
3. Ganti String concatenation dengan Stream dan Collectors.joining()
4. Ganti loop manual pencarian max GPA dengan database query langsung

Memastikan fungsionalitas tidak rusak:
1. Verifikasi response data sama persis (struktur dan konten)
2. Gunakan integration test untuk endpoint yang dioptimasi
3. Lakukan code review untuk memastikan logika bisnis tidak berubah
4. Bandingkan hasil JMeter before dan after untuk konfirmasi improvement
