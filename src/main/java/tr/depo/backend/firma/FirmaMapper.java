package tr.depo.backend.firma;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.DegerNesnesiEsleyici;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.firma.dto.CreateFirmaRequest;
import tr.depo.backend.firma.dto.FirmaResponse;
import tr.depo.backend.firma.dto.UpdateFirmaRequest;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Firma ↔ DTO. Entity türe göre domain fabrikasıyla oluşur; türün zorunlu alanları servis tarafından önceden
 * doğrulanır. Yetkili/irtibat kişisi API'de düz alanlardır.
 */
@Mapper(config = MapperYapilandirmasi.class)
public interface FirmaMapper {

    @Mapping(target = "yetkiliAd", source = "yetkili.ad")
    @Mapping(target = "yetkiliTelefon", source = "yetkili.telefon")
    @Mapping(target = "irtibatAd", source = "irtibat.ad")
    @Mapping(target = "irtibatTelefon", source = "irtibat.telefon")
    FirmaResponse cevap(Firma firma);

    @Mapping(target = "depo", ignore = true)
    @Mapping(target = "yetkili", ignore = true)
    @Mapping(target = "irtibat", ignore = true)
    Firma yeni(CreateFirmaRequest istek);

    @Mapping(target = "depo", ignore = true)
    @Mapping(target = "yetkili", ignore = true)
    @Mapping(target = "irtibat", ignore = true)
    void guncelle(UpdateFirmaRequest istek, @MappingTarget Firma firma);

    @ObjectFactory
    default Firma yeniFirma(CreateFirmaRequest istek) {
        return istek.tur() == MalzemeTuru.SARF
            ? Firma.sarfFirmasi(istek.ad())
            : Firma.demirbasFirmasi(istek.durumKodu(), istek.kayitTarihi());
    }

    @AfterMapping
    default void iletisimKisileri(CreateFirmaRequest istek, @MappingTarget Firma firma) {
        firma.setYetkili(DegerNesnesiEsleyici.iletisimKisisi(istek.yetkiliAd(), istek.yetkiliTelefon()));
        firma.setIrtibat(DegerNesnesiEsleyici.iletisimKisisi(istek.irtibatAd(), istek.irtibatTelefon()));
    }

    @AfterMapping
    default void iletisimKisileri(UpdateFirmaRequest istek, @MappingTarget Firma firma) {
        firma.setYetkili(DegerNesnesiEsleyici.iletisimKisisi(istek.yetkiliAd(), istek.yetkiliTelefon()));
        firma.setIrtibat(DegerNesnesiEsleyici.iletisimKisisi(istek.irtibatAd(), istek.irtibatTelefon()));
    }
}
