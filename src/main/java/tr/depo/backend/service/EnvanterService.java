package tr.depo.backend.service;

import java.time.LocalDateTime;
import tr.depo.domain.envanter.Envanter;

/**
 * Envanter aggregate'inin demirbaş ve sarf servislerince paylaşılan işleri. Envanterin kendi endpoint'i yoktur:
 * API'de {@code /demirbaslar} ve {@code /sarf-malzemeleri} olarak ayrı kaynaklardır.
 */
public interface EnvanterService {

    /**
     * Tanım ilişkilerini ID'den çözer ve bağlar (kategori, marka, model envanterle aynı türde olmalı; model seçilen
     * markaya ait olmalı, marka boşsa modelin markası atanır). Herhangi biri geçersizse hiçbiri bağlanmadan 422.
     */
    void bagla(Envanter envanter, Long kategoriId, Long markaId, Long modelId, Long depoId, Long ihaleId);

    /**
     * Güncellemede {@link #bagla}: ek olarak kayıtta dolu olan kategori, marka ya da model boşaltılamaz (422). Legacy'den
     * boş gelen bir tanım boş kalabilir; kayıt yine güncellenebilir. Yeni kayıtta üçü de istek doğrulamasıyla zorunludur.
     */
    void guncellemedeBagla(Envanter envanter, Long kategoriId, Long markaId, Long modelId, Long depoId, Long ihaleId);

    /** Garanti bitişi başlangıçtan önce olamaz (422). */
    void garantiSirasiniDogrula(LocalDateTime baslangic, LocalDateTime bitis);

    /**
     * Envanter silinirken alt kayıtlarını (barkod, not, aparat, fotoğraf) aynı transaction'da soft delete eder.
     * Hareket, arıza ve sayım satırları ayrı aggregate'lerdir (geçmiş kaydı); envanterle birlikte silinmez.
     */
    void altKayitlariSil(Envanter envanter, String silen);
}
