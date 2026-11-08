# kaju-backend

Depo / envanter uygulamasının CRUD backend'i. Domain modeli (JPA entity'leri ve Flyway şeması) bu repoda
değil, **`tr.depo:depo-domain`** bağımlılığındadır; tek kaynağı
[kaju-migration](https://github.com/acartunahan/kaju-migration) reposudur. Domain kararları için oradaki
`HANDOFF.md`'ye bakın.

| | |
|---|---|
| Dil / platform | Java 25, Spring Boot 4.0.9 (Spring Framework 7, Hibernate 7.2) |
| Veritabanı | Oracle (şema domain'deki Flyway script'leriyle; `ddl-auto=validate`) |
| API | REST, `/api/v1`, RFC 9457 ProblemDetail, OpenAPI (`/swagger-ui.html`) |
| Mapping | MapStruct 1.6.3 |
| Test | JUnit 5, Mockito, AssertJ, ArchUnit, Testcontainers (Oracle Free 23) |

## Domain bağımlılığı

`depo-domain` kaju-migration reposundan yayımlanır:

- **Yerelde (en hızlısı):** kaju-migration klasöründe
  ```bash
  cd depo-modernizasyon
  mvn -B -pl depo-domain -am install
  ```
  Jar `~/.m2`'ye kurulur; bu proje önce oradan okur.
- **GitHub Packages:** kaju-migration'da `domain-v<sürüm>` etiketi push edilince yayımlanır. Bu projeyi
  başka bir makinede derlemek için `~/.m2/settings.xml`'e `read:packages` yetkili bir token ekleyin:
  ```xml
  <settings>
    <servers>
      <server>
        <id>github</id>
        <username>GITHUB_KULLANICI_ADI</username>
        <password>${env.GITHUB_TOKEN}</password>
      </server>
    </servers>
  </settings>
  ```
  CI bunu `GITHUB_TOKEN` ile yapar; paket ayarlarında bu repoya Actions erişimi verilmiş olmalı.

Domain değişikliği bu repoda yapılmaz: kaju-migration'da yeni bir Flyway sürümüyle yapılır, yayımlanır ve
buradaki `depo-domain.version` yükseltilir.

## Çalıştırma

Profiller: `local` (varsayılan), `test`, `prod`. Bağlantı bilgileri ortam değişkenlerinden okunur:

| Değişken | local varsayılanı | prod |
|---|---|---|
| `DB_URL` | `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` | zorunlu |
| `DB_KULLANICI` | `HEDEF` | zorunlu |
| `DB_SIFRE` | — (zorunlu) | zorunlu |
| `SWAGGER_ACIK` | — | `false` |

Yerel veritabanı kaju-migration'daki Docker Compose'tur (`depo-modernizasyon/yerel-db`). Şemayı kuran
kullanıcının `CREATE TRIGGER` yetkisi olmalıdır (domain'deki fiziksel silme yasağı trigger'ları).

```bash
DB_SIFRE=... mvn spring-boot:run
```

- API: `http://localhost:8080/api/v1/...`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Sağlık: `http://localhost:8080/actuator/health`

## Testler

```bash
mvn -B test      # birim + web katmanı + mimari kurallar (Docker gerekmez)
mvn -B verify    # + *EntegrasyonTesti: Testcontainers ile gerçek Oracle (Docker gerekir)
```

## API sözleşmesi

- **Kaynaklar:** `GET /{kaynak}/{id}`, `GET /{kaynak}` (sayfalı), `POST` (201 + `Location`), `PUT /{id}`
  (tam güncelleme, `versiyon` zorunlu), `DELETE /{id}` (204).
- **Silme her zaman soft delete'tir.** Silinmiş kayıt GET/PUT/DELETE'te 404 döner, listelerde görünmez;
  silinmiş kayda referans verilemez (422). Başka aktif kayıtların kullandığı bir kayıt silinemez (409).
- **Sayfalama:** `?page=0&size=20&sort=ad,asc`; boyut üst sınırı 100; sıralama yalnızca kaynağın izin verdiği
  alanlarla (aksi halde 400). Cevap: `{icerik, sayfa, boyut, toplamEleman, toplamSayfa}`.
- **Hatalar (RFC 9457):** `type`, `title`, `status`, `detail`, `instance` ve ek olarak `kod`; doğrulama
  hatalarında `hatalar: [{alan, mesaj}]`, referans hatalarında `alan`. Mesajlar Türkçe (`messages.properties`).

| Durum | Kod |
|---|---|
| 400 | `DOGRULAMA_HATASI`, `GECERSIZ_ISTEK`, `GECERSIZ_SIRALAMA` |
| 404 | `KAYIT_BULUNAMADI` (yok ya da silinmiş), `KAYNAK_BULUNAMADI` |
| 409 | `SURUM_CAKISMASI`, `TEKILLIK_IHLALI`, `KAYIT_KULLANIMDA` |
| 422 | `REFERANS_GECERSIZ`, `IS_KURALI_IHLALI`, `VERI_KURALI_IHLALI` |
| 500 | `BEKLENMEYEN_HATA` (ayrıntı sadece logda) |

## Kaynaklar

Tümü `/api/v1` altında. "Silinince 409" = başka aktif kayıtlar kullanıyorsa silme engellenir; "birlikte silinir" =
aggregate'in alt kayıtları aynı transaction'da soft delete edilir.

| Kaynak | Yol | İşlemler | Silme |
|---|---|---|---|
| Depo | `/depolar` | CRUD | 409: depoya bağlı aktif kayıt |
| Kategori | `/kategoriler` | CRUD (tur; üst aynı türde, döngü yok) | 409: alt kategori, model, envanter |
| Marka | `/markalar` | CRUD (tur) | 409: model, envanter |
| Model | `/modeller` | CRUD (tur; yeni modelde marka zorunlu, güncellemede dolu marka boşaltılamaz) | 409: envanter |
| Firma | `/firmalar` | CRUD (tur) | 409: ihale |
| İhale | `/ihaleler` | CRUD (yalnızca demirbaş firması) | 409: envanter |
| Demirbaş | `/demirbaslar` | CRUD; `GET /demirbaslar?zimmetliSicil=…` o sicile şu an zimmetli olanlar (demirbaş no ve seri no aktif demirbaşlarda tekil: 409; yeni kayıtta kategori, marka ve model zorunlu, güncellemede dolu olan boşaltılamaz; model yalnızca kendi markasıyla) | barkod, not, aparat, fotoğraf birlikte silinir |
| Sarf malzemesi | `/sarf-malzemeleri` | CRUD (yeni kayıtta kategori, marka ve model zorunlu, güncellemede dolu olan boşaltılamaz; model yalnızca kendi markasıyla) | barkod, not, aparat, fotoğraf birlikte silinir |
| Barkod / not / aparat | `/envanterler/{envanterId}/barkodlar` · `/notlar` · `/aparatlar` | CRUD | — |
| Arıza | `/arizalar` | CRUD | işlemler birlikte silinir |
| Arıza işlemi | `/arizalar/{arizaId}/islemler` | CRUD | — |
| Sayım | `/sayimlar` | CRUD | satırlar birlikte silinir |
| Sayım satırı | `/sayimlar/{sayimId}/satirlar` | CRUD (tur: YEREL/GENEL, sayımın kapsamıyla aynı) | — |
| Envanter hareketi | `/envanter-hareketleri` | Yalnızca GET, POST (geçmiş kaydı) | Yok (405) |
| Zimmet kaydı | `/zimmet-kayitlari` | Yalnızca GET, POST (geçmiş kaydı) | Yok (405) |
| Zimmet geçmişi | `/demirbaslar/{demirbasId}/zimmet-gecmisi` | Yalnızca GET (en yeni önce). Zimmetli sicil değişince demirbaş servisi otomatik yazar; migration başlangıç kaydını yazar | Yok (405) |

Bilerek açılmayanlar: `COKLU_ZIMMET` ve `SQL_KAYDI` (anlamları doğrulanmadı), envanter fotoğrafı (dosya deposu
bağlantısı sonra).

## Yapı

Katman bazlı, düz paketler (şirket standardı); her entity için `XController`, `XService`, `XServiceImpl`,
`XRepository`, `XMapper`, `CreateXRequest`, `UpdateXRequest`, `XResponse`. Testler aynı paketleri izler.

| Paket | İçerik |
|---|---|
| `controller` | Tüm controller'lar; `ApiPaths` (sürüm öneki ve envanter alt kaynaklarının üst yolu) |
| `service` | Servis arayüzleri; ortak olanlar `EnvanterService`, `ReferansService` (aktif referans yükleme), `SilmeService` |
| `serviceimpl` | Servis implementasyonları; `AbstractSoftDeleteServiceImpl` (silme akışı), `AbstractAltKayitServiceImpl` (alt kaynaklar) |
| `repository` | Repository'ler; taban `AktifKayitRepository` (delete* yok) |
| `dto` | İstek/cevap DTO'ları; `SayfaResponse`, `IliskiOzetiResponse`, `KullanimReferansi` |
| `mapper` | MapStruct mapper'ları; `IliskiOzetiMapper`, `DegerNesnesiMapper` |
| `exception` | `KajuException` hiyerarşisi, `HataKodu`, `GlobalExceptionHandler`, `OracleHataCevirici` |
| `config` | JPA (entity taraması, UTC audit zamanı), geçici `AuditorAware`, OpenAPI, MapStruct ortak ayarı |
| `util` | `AktifKayitUtil` (silinmemiş kayıt ya da 404), `SiralamaDogrulayici`, `KaynakKonumu` |

Mimari kurallar `MimariKurallarTest` (ArchUnit) ile build'de zorlanır: repository'ler `delete*` açan
arayüzlerden türeyemez, `EntityManager.remove` ve `DELETE` sorgusu yasak, controller entity ve
`ServiceImpl`/repository'ye bağlanamaz, alan enjeksiyonu yok.
