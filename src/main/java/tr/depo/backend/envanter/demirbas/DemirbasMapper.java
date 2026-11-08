package tr.depo.backend.envanter.demirbas;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDateTime;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.envanter.demirbas.dto.CreateDemirbasRequest;
import tr.depo.backend.envanter.demirbas.dto.DemirbasResponse;
import tr.depo.backend.envanter.demirbas.dto.UpdateDemirbasRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.GarantiSuresi;

/**
 * Demirbaş ↔ Envanter. {@code durumKodu} sarf malzemesine ait legacy alanıdır, demirbaşta eşlenmez. İlişkiler servis
 * tarafından bağlanır; garanti API'de iki düz tarih alanıdır.
 */
@Mapper(config = MapperYapilandirmasi.class)
public interface DemirbasMapper {

    @Mapping(target = "garantiBaslangic", source = "garanti.baslangic")
    @Mapping(target = "garantiBitis", source = "garanti.bitis")
    DemirbasResponse cevap(Envanter envanter);

    @ServisinBagladigiAlanlar
    Envanter yeni(CreateDemirbasRequest istek);

    @ServisinBagladigiAlanlar
    void guncelle(UpdateDemirbasRequest istek, @MappingTarget Envanter envanter);

    @ObjectFactory
    default Envanter yeniDemirbas(CreateDemirbasRequest istek) {
        return Envanter.demirbas(istek.durum(), istek.kayitTarihi());
    }

    @AfterMapping
    default void garanti(CreateDemirbasRequest istek, @MappingTarget Envanter envanter) {
        envanter.setGaranti(garanti(istek.garantiBaslangic(), istek.garantiBitis()));
    }

    @AfterMapping
    default void garanti(UpdateDemirbasRequest istek, @MappingTarget Envanter envanter) {
        envanter.setGaranti(garanti(istek.garantiBaslangic(), istek.garantiBitis()));
    }

    private static GarantiSuresi garanti(LocalDateTime baslangic, LocalDateTime bitis) {
        return baslangic == null && bitis == null ? null : new GarantiSuresi(baslangic, bitis);
    }

    /** Servisin ayrıca bağladığı (ilişkiler, garanti) ve demirbaşta kullanılmayan (durumKodu) alanlar. */
    @Retention(RetentionPolicy.CLASS)
    @Mapping(target = "durumKodu", ignore = true)
    @Mapping(target = "garanti", ignore = true)
    @Mapping(target = "kategori", ignore = true)
    @Mapping(target = "marka", ignore = true)
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "depo", ignore = true)
    @Mapping(target = "ihale", ignore = true)
    @interface ServisinBagladigiAlanlar {
    }
}
