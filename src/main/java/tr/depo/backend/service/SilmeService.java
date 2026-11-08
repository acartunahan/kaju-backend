package tr.depo.backend.service;

import tr.depo.backend.dto.KullanimReferansi;
import tr.depo.domain.ortak.BaseEntity;

/**
 * Soft delete akışının entity'den bağımsız parçaları: silen kullanıcıyı bulmak, alt kayıtları soft delete etmek ve
 * aktif referansları saymak.
 */
public interface SilmeService {

    /** Silme işlemini yapan kullanıcı (SILEN kolonu); audit alanlarıyla aynı kaynaktan. */
    String silen();

    /**
     * Aggregate root silinirken alt kayıtlarını aynı transaction'da soft delete eder.
     *
     * @return silinen alt kayıt sayısı
     */
    int aktifAltKayitlariSil(Class<? extends BaseEntity> tip, String ustAlani, BaseEntity ust, String silen);

    /** Hedef kayda referans veren aktif (silinmemiş) kayıt sayısı. */
    long aktifReferansSayisi(KullanimReferansi referans, BaseEntity hedef);
}
