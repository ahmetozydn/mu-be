# Mobile Developer Mülakat Soruları (Senior & Lead Seviye)

Bu sorular, adayların sadece teknik bilgisini değil, aynı zamanda mühendislik kararlarındaki muhakeme yeteneğini, mimari tercihlerini ve platformların derinliklerindeki darboğazları anlama seviyesini ölçmek için tasarlanmıştır.

---

### 1. Soru: Native vs. Cross-Platform Kararı ve "Hidden Costs"
**Soru:** Yeni bir proje için teknoloji seçimi yaparken, React Native veya Flutter gibi cross-platform çözümlerin "pazarlanan" hızı ile gerçek hayattaki "bakım ve özelleştirme" maliyetleri arasındaki dengeyi nasıl kurarsınız? Hangi durumlarda Native (Swift/Kotlin) kaçınılmaz bir zorunluluktur?

**Cevap (Tartışmalı Noktalar):** 
Bu sorunun cevabı net bir "A iyidir" değildir. Cross-platform çözümler %80-90 kod paylaşımı vaat etse de, kalan %10-20'lik kısım (Native Bridge, Platform-Specific UI, OS-level API entegrasyonları) toplam sürenin %50'sini alabilir.
- **Tartışma:** "Single Codebase" bir yalan mı? Cross-platform kullanıldığında aslında üç platform öğrenmek zorunda kalıyorsunuz: Framework (Flutter/RN), iOS ve Android.
- **Native Zorunluluğu:** Düşük gecikmeli ses/video işleme, yoğun Bluetooth/Hardware iletişimi veya OS'in sunduğu en yeni özellikleri (örneğin Dynamic Island veya yeni Android widget'ları) anında kullanma gereksinimi native'e zorlar.
- **Derinlik:** Cevapta "Native Bridge bottleneck" ve "Platform Channels" üzerinden performans tartışması beklenir.

### 2. Soru: Declarative UI ve State Management Karmaşası
**Soru:** SwiftUI veya Jetpack Compose gibi modern declarative UI framework'lerinde, State'in "Recomposition" veya "Redraw" döngüsünü nasıl optimize edersiniz? Gereksiz render'ları önlemek için kullandığınız stratejiler nelerdir ve bu yapıların karmaşık animasyonlarda yarattığı performans sorunlarını nasıl aşarsınız?

**Cevap (Tartışmalı Noktalar):** 
Declarative UI, state değişiminde tüm ağacı kontrol etme eğilimindedir. 
- **Tartışma:** "State Hoisting" her zaman iyi midir? State'i çok yukarı taşımak, hiyerarşideki ilgisiz component'lerin de render edilmesine sebep olur. 
- **Teknik Detay:** Swift'te `@Observable` vs `@StateObject` farkları, Compose'da `derivedStateOf` veya `remember` kullanımının yanlış yapılması durumunda memory leak veya UI jank oluşumu. 
- **Çekişme:** Performans için UI ağacını parçalamak mı, yoksa state yönetimini merkezi (Redux/MVI) tutmak mı daha mantıklı?

### 3. Soru: Dependency Injection (DI) - Hilt/Dagger vs. Swinject vs. Manual Injection
**Soru:** Mobil uygulamalarda DI kullanımı test edilebilirlik için kritiktir. Ancak Dagger gibi ağır kütüphanelerin build süresine etkisi ve Swinject gibi runtime tabanlı çözümlerin crash riskleri arasında bir seçim yapmanız gerekse, tercihinizi neye göre yaparsınız? Service Locator pattern'i bir "anti-pattern" midir?

**Cevap (Tartışmalı Noktalar):** 
DI kütüphaneleri "magic" yaratır ancak debugging'i zorlaştırır.
- **Tartışma:** Service Locator (örneğin Koin veya GetIt) kullanımı junior ekipler için hızlıdır ama compile-time güvenliği yoktur. 
- **Derinlik:** "Pure DI" (kütüphanesiz DI) büyük projelerde sürdürülebilir mi? Build time (Dagger/Hilt'in annotation processing süresi) vs Runtime Safety dengesi.

### 4. Soru: Offline-First Mimari ve Veri Tutarlılığı (Data Consistency)
**Soru:** Uygulamanızın tamamen çevrimdışı çalışabildiği bir senaryoda, sunucu ile yerel veritabanı (Room/Realm/CoreData) arasındaki senkronizasyonu nasıl yönetirsiniz? "Conflict Resolution" (çatışma çözümü) stratejileriniz nelerdir ve kullanıcının yaptığı bir değişikliğin sunucuda başarısız olması durumunda UI tutarlılığını (Optimistic UI) nasıl sağlarsınız?

**Cevap (Tartışmalı Noktalar):** 
Optimistic UI (değişikliği hemen gösterme) kullanıcı deneyimi için harikadır ama rollback durumunda kafa karıştırıcıdır.
- **Tartışma:** Last-Write-Wins (LWW) stratejisi yeterli mi? Yoksa vektör saatleri veya CRDT (Conflict-free Replicated Data Types) gibi karmaşık yapılar mı kullanılmalı?
- **Detay:** Arka planda sync işlemleri sırasında pil tüketimi (WorkManager/BackgroundTasks) ve throttling limitleri.

### 5. Soru: Uygulama Boyutu (Binary Size) ve Dinamik Modüller
**Soru:** Uygulama boyutunun dönüşüm oranlarını (conversion rates) doğrudan etkilediği biliniyor. 150MB'lık bir uygulamayı 50MB'a düşürmeniz istendiğinde, "Dynamic Delivery" (Android App Bundles) veya "On-Demand Resources" (iOS) dışındaki derin teknikleriniz neler olurdu? Image optimizasyonunun ötesine nasıl geçersiniz?

**Cevap (Tartışmalı Noktalar):** 
Sadece asset'leri silmek yetmez. 
- **Tartışma:** Üçüncü parti kütüphanelerin (SDKs) maliyeti. Bir SDK, bazen uygulamanın %30'unu kaplayabilir. 
- **Teknik:** LTO (Link Time Optimization), Dead code elimination, obfuscation (ProGuard/R8) ayarlarının agresifleşmesi ve bunların yarattığı runtime crash riskleri.

### 6. Soru: Memory Management: ARC vs. Garbage Collection
**Soru:** iOS'teki ARC (Automatic Reference Counting) ile Android'deki GC (Garbage Collection) arasındaki temel farkların, bir geliştiricinin memory leak avcılığı stratejisini nasıl değiştirdiğini açıklayın. "Retain Cycle" sadece iOS'e özgü bir sorun mudur?

**Cevap (Tartışmalı Noktalar):** 
ARC deterministiktir (obje hemen silinir), GC ise değildir. 
- **Tartışma:** Android'de `Context` sızıntıları, iOS'te `self` yakalamaları (closures). 
- **Derinlik:** Kotlin Coroutines veya Swift Concurrency (async/await) kullanımının bellek yönetimine etkisi. Yapılandırılmamış (unstructured) concurrency'nin yarattığı "zombi" objeler.

### 7. Soru: UI Thread ve "Jank" Analizi
**Soru:** Uygulamanızda 60 FPS (veya 120 FPS) yakalamaya çalışırken listelerde (RecyclerView/UICollectionView) kaydırma sırasında takılmalar (jank) fark ediyorsunuz. Profiler (Instruments/Android Studio Profiler) kullandığınızda CPU'nun değil, main thread'in bloklandığını görüyorsunuz. Karmaşık bir "Cell" yapısında layout hesaplamalarını nasıl optimize edersiniz?

**Cevap (Tartışmalı Noktalar):** 
Sadece "view recycling" yetmez. 
- **Tartışma:** Prefetching stratejileri. Görüntülerin render edilmeden önce decompress edilmesi (Image decoding bottleneck). 
- **Teknik:** Auto-layout'un maliyeti. Manuel frame hesaplaması (layoutSubviews/onLayout) ne zaman tercih edilmeli?

### 8. Soru: Modularization ve Build Speed
**Soru:** Uygulamanız 50+ modüle ulaştığında build süreleri 10 dakikayı geçmeye başladı. "Layer-based" modülerleşme (Data, Domain, UI) yerine "Feature-based" modülerleşmeye geçmenin avantajları nelerdir? Modüller arası iletişimde (Navigation/Data sharing) "tight coupling"den nasıl kaçınırsınız?

**Cevap (Tartışmalı Noktalar):** 
Her şeyin modüllere ayrılması build süresini bazen daha da artırabilir (graph complexity). 
- **Tartışma:** "Micro-apps" yaklaşımı. Core modülünün her şeye bağımlı olması (God module) sorunu. 
- **Teknik:** Remote caching (Bazel/Buck/Tuist) ve bunun kurulum maliyeti.

### 9. Soru: Mobile Security ve "Root/Jailbreak" Paradoksu
**Soru:** Finansal bir uygulama geliştiriyorsunuz. Uygulamanızın root edilmiş veya jailbreak yapılmış cihazlarda çalışmasına izin vermeli misiniz? "Certificate Pinning"in günümüzde yarattığı riskler (sertifika süresi dolması/rotasyon zorlukları) ve bypass edilme kolaylığı hakkında ne düşünüyorsunuz?

**Cevap (Tartışmalı Noktalar):** 
Güvenlik vs Kullanılabilirlik. 
- **Tartışma:** Jailbreak tespiti hiçbir zaman %100 değildir (Cat and mouse game). 
- **Derinlik:** Biometrik kimlik doğrulamanın güvenliği (Tee/Secure Enclave). Verilerin keychain'de tutulmasının yeterliliği.

### 10. Soru: Modern Concurrency: Swift Actors vs. Kotlin Flow/Channels
**Soru:** Çoklu thread yönetiminde "Race Condition" ve "Deadlock"ları önlemek için modern dillerin sunduğu yaklaşımları (Swift'teki `Actor` yapısı veya Kotlin'deki `Flow/Channels`) kıyaslayın. Paylaşılan mutable state'i yönetirken "Thread-safe" olmanın maliyeti (performance overhead) nedir?

**Cevap (Tartışmalı Noktalar):** 
Lock'lar (Mutex/Semaphores) yavaştır. 
- **Tartışma:** "Don't communicate by sharing memory; share memory by communicating" prensibi mobil dünyada ne kadar uygulanabilir? 
- **Teknik:** Actor isolation'ın yarattığı context switching maliyeti. Flow'un (Cold stream) Channels'a (Hot stream) tercih edilmesi gereken durumlar.

---
**Not:** Bu soruların cevapları adayın deneyimine göre derinleşmelidir. Cevap verirken "Depende" (duruma göre değişir) diyebilen ve nedenlerini açıklayan aday genellikle senior/lead profildir.
