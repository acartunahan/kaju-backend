package tr.depo.backend.util;

/**
 * Bir envanterin zorunlu tanım ilişkileri (kategori, marka ve o markanın modeli). Ürün oluşturulurken üçü de
 * zorunludur; entegrasyon testlerinde {@link VeritabaniFixture} gerçek kayıtlarla, web testlerinde sabit ID'lerle
 * kurulur.
 */
public record TanimSeti(long kategoriId, long markaId, long modelId) {

    /** Web testleri için: servis mock'landığı için ID'lerin var olması gerekmez. */
    public static final TanimSeti SAHTE = new TanimSeti(1L, 2L, 3L);

    IstekGovdesi uygula(IstekGovdesi govde) {
        return govde.ile("kategoriId", kategoriId).ile("markaId", markaId).ile("modelId", modelId);
    }
}
