package tr.depo.backend.sayim.satir;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.sayim.satir.dto.CreateSayimSatiriRequest;
import tr.depo.backend.sayim.satir.dto.SayimSatiriResponse;
import tr.depo.backend.sayim.satir.dto.UpdateSayimSatiriRequest;
import tr.depo.domain.sayim.MalzemeAnlikGorunumu;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.SayimSatiri;

/**
 * Sayım satırı ↔ DTO. Satır türe göre domain fabrikasıyla oluşur ({@code yerel} / {@code genel}); türün kuralları
 * servis tarafından önceden doğrulanır. Anlık malzeme bilgisi API'de dört düz alandır.
 */
@Mapper(config = MapperYapilandirmasi.class)
public interface SayimSatiriMapper {

    @Mapping(target = "sayimId", source = "sayim.id")
    @Mapping(target = "anlikAd", source = "anlikGorunum.ad")
    @Mapping(target = "anlikMarka", source = "anlikGorunum.marka")
    @Mapping(target = "anlikModel", source = "anlikGorunum.model")
    @Mapping(target = "anlikSeriNo", source = "anlikGorunum.seriNo")
    SayimSatiriResponse cevap(SayimSatiri satir);

    @ServisinBagladigiAlanlar
    SayimSatiri yeni(CreateSayimSatiriRequest istek);

    @ServisinBagladigiAlanlar
    void guncelle(UpdateSayimSatiriRequest istek, @MappingTarget SayimSatiri satir);

    @ObjectFactory
    default SayimSatiri yeniSatir(CreateSayimSatiriRequest istek) {
        return istek.tur() == SayimKapsami.YEREL
            ? SayimSatiri.yerel(istek.malzemeEskiRef(), istek.zimmetEskiRef(), istek.islem(), istek.malzemeTuru())
            : SayimSatiri.genel(istek.islem(), istek.islemTarihi());
    }

    @AfterMapping
    default void anlikGorunum(CreateSayimSatiriRequest istek, @MappingTarget SayimSatiri satir) {
        satir.setAnlikGorunum(anlikGorunum(istek.anlikAd(), istek.anlikMarka(), istek.anlikModel(), istek.anlikSeriNo()));
    }

    @AfterMapping
    default void anlikGorunum(UpdateSayimSatiriRequest istek, @MappingTarget SayimSatiri satir) {
        satir.setAnlikGorunum(anlikGorunum(istek.anlikAd(), istek.anlikMarka(), istek.anlikModel(), istek.anlikSeriNo()));
    }

    private static MalzemeAnlikGorunumu anlikGorunum(String ad, String marka, String model, String seriNo) {
        return ad == null && marka == null && model == null && seriNo == null
            ? null
            : new MalzemeAnlikGorunumu(ad, marka, model, seriNo);
    }

    /** Servisin bağladığı ilişkiler ve AfterMapping ile kurulan anlık görünüm. */
    @Retention(RetentionPolicy.CLASS)
    @Mapping(target = "sayim", ignore = true)
    @Mapping(target = "envanter", ignore = true)
    @Mapping(target = "depo", ignore = true)
    @Mapping(target = "anlikGorunum", ignore = true)
    @interface ServisinBagladigiAlanlar {
    }
}
