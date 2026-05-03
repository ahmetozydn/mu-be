# Soru Üretim Kılavuzu (JSON Formatı)

Bu belge, mülakat hazırlık platformu için soru üretim standartlarını belirler. Soruların binlerce adede ulaşacağı varsayıldığından, tüm veriler aşağıdaki JSON formatına ve kalite standartlarına sıkı sıkıya bağlı kalmalıdır.

## 4. Dosya Yapısı ve İsimlendirme (File Structure & Naming)

Tüm soru dosyaları projenin kaynak dizini altında toplanmalı ve isimlendirme kurallarına uyulmalıdır:

- **Dizin:** `src/main/resources/data/`
- **İsimlendirme Formatı:** `[kategori_id]_[dil].json`
    - Örnek: `mobile_tr.json`, `android_en.json`, `frontend_tr.json`

---

## 5. JSON Yapısı (Schema)

Her soru grubu bir dizi (array) içinde, aşağıdaki objelerden oluşmalıdır:

```json
[
  {
    "category": "kategori_id",
    "language": "tr | en",
    "difficulty": "junior | mid | senior",
    "question": "Soru metni buraya gelecek...",
    "options": [
      "Seçenek 0",
      "Seçenek 1",
      "Seçenek 2",
      "Seçenek 3"
    ],
    "correctIndex": 0,
    "explanation": "Detaylı teknik açıklama buraya gelecek."
  }
]
```

## 1. Dosya Yapısı ve Kök Element (Root Element)

Her JSON dosyası **mutlaka** tek bir JSON dizisi (array) ile başlamalı ve bitmelidir. Dosya içerisinde birden fazla dizi veya dizi dışında bir kök element bulunamaz.

```json
[
  { ... soru 1 ... },
  { ... soru 2 ... }
]
```

## 2. Kalite Standartları ve Seviyeler

### Kaynaklar ve Gerçekçilik
- **Geniş Kaynak Yelpazesi:** Sorular; Google, Amazon, Meta, Apple gibi teknoloji devlerinin mülakat süreçlerinden, popüler teknoloji bloglarından (Medium, Engineering Blogs), mülakat hazırlık sitelerinden (LeetCode, Glassdoor, InterviewBit) veya gerçek iş mülakatı deneyimlerinden türetilmelidir. 

### Tecrübe Seviyeleri (Difficulty)
- **Junior (0-2 Yıl):** İlk iş deneyimi veya kariyerinin başında olan adaylar içindir. Temel dil bilgisi, temel framework kavramları ("Nedir?", "Nasıl çalışır?") ve temel problem çözme yeteneği sorgulanır.
- **Mid (2-5 Yıl):** Belirli bir derinliğe sahip, best practice'leri uygulayan adaylar içindir. Uygulama mimarisi, hata yönetimi, performans optimizasyonu ve yaygın kullanılan kütüphanelerin iç yapısı sorgulanır.
- **Senior (5+ Yıl):** Geniş sistem tasarımı perspektifine sahip adaylar içindir. Karmaşık mimari kararlar, trade-off (ödünleşim) analizleri, sistem güvenliği, scalability (ölçeklenebilirlik) ve derin teknik iç işleyiş sorgulanır.

### Dil Bilgisi
- Teknik terimler yerleşik kullanımına göre (Örn: "Recomposition", "Deadlock", "Main Thread") bırakılmalı, geri kalan metin akıcı ve anlaşılır bir dilde yazılmalıdır.

### Şıklar ve Çeldiriciler
- **Belirsizlik İlkesi:** Doğru cevap ilk bakışta "kabak gibi" belli olmamalıdır.
- **Yakınlık:** Diğer 3 şık teknik olarak mantıklı tınlamalı ancak bir nüans, bir istisna veya bir yan etki nedeniyle yanlış olmalıdır. 
- **Uzunluk ve Denge:** Şıkların uzunlukları birbirine yakın olmalıdır. **En uzun şık her zaman doğru cevap olmamalıdır.**
- **Rastgelelik:** Doğru cevabın index'i (`correctIndex`) ve şıkların uzunluk dağılımı rastgele olmalıdır. Belirli bir pattern (Örn: Hep 2. şıkkın uzun olması) oluşmamalıdır.

### Açıklamalar (Explanation)
- Sadece doğru cevabı değil, yanlış şıkların neden yanlış olduğunu veya konunun temel mantığını (Örn: Bellek yönetimi, CPU maliyeti) 2-3 cümleyle özetlemelidir.

## 3. Örnek Soru (İdeal Form)

```json
{
  "category": "mobile",
  "language": "tr",
  "difficulty": "senior",
  "question": "Android'de bir Activity 'onSaveInstanceState()' metodunda büyük bir Bitmap verisini Bundle'a koyarsa, uygulama arka plana atıldığında hangi sorunla karşılaşma riski en yüksektir?",
  "options": [
    "Uygulama anında OutOfMemoryError (OOM) vererek çöker.",
    "İşlemcinin (CPU) aşırı ısınması sonucu sistem uygulamayı sonlandırır.",
    "TransactionTooLargeException hatası alınır ve uygulama crash olur.",
    "Bitmap verisi otomatik olarak disk belleğine (cache) yazıldığı için sadece performans düşer."
  ],
  "correctIndex": 2,
  "explanation": "Android'de Bundle verileri binder transaction'ları üzerinden taşınır ve bu buffer limiti yaklaşık 1MB'dır. Büyük verilerin (Bitmap gibi) bu limitin aşılmasına neden olması TransactionTooLargeException fırlatır."
}
```

## 5. Teknik Derinlik Kalibrasyonu (Depth Calibration)

Soru derinliği, kategorinin kapsamına göre ayarlanmalıdır:

- **Genel Pozisyonlar (Örn: `mobile`, `frontend`, `backend`):** 
    - Mimari desenler (MVVM, Clean Architecture), trade-off analizleri (Native vs Cross), genel güvenlik ve performans stratejileri.
    - Çok derin kütüphane detaylarından kaçınılmalı, "mühendislik muhakemesi" ölçülmelidir.
- **Platform Bazlı (Örn: `android`, `ios`):**
    - İşletim sistemi mimarisi, bellek yönetimi (ARC vs GC), thread yönetimi ve OS-specific API'ler.
- **Stack/Framework Bazlı (Örn: `flutter`, `react-native`, `react`):**
    - Framework iç yapısı, render döngüleri, state management kütüphanelerinin çalışma mantığı ve framework'e özgü darboğazlar.

## 6. Kategori Listesi (Granular IDs)

- **Genel:** `frontend`, `backend`, `fullstack`, `mobile`, `devops`, `hr`.
- **Teknik/Konsept:** `algorithms`, `system-design`, `database`, `security`, `testing`.
- **Platform/Stack:** `android`, `ios`, `flutter`, `react-native`, `react`, `vue`, `angular`, `nodejs`, `spring-boot`.
- **Diller:** `javascript`, `typescript`, `python`, `java`, `csharp`, `go`, `rust`, `kotlin`, `swift`, `php`.
