package tr.depo.backend.envanter.sarf;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.envanter.sarf.dto.CreateSarfMalzemesiRequest;
import tr.depo.backend.envanter.sarf.dto.SarfMalzemesiResponse;
import tr.depo.backend.envanter.sarf.dto.UpdateSarfMalzemesiRequest;
import tr.depo.domain.envanter.Envanter;

/**
 * Sarf malzemesi ↔ Envanter. Demirbaşa özgü alanlar (demirbaş no, seri no, durum, garanti, zimmet…) sarfta
 * kullanılmaz; açıkça yok sayılır ki yeni bir alan eklendiğinde derleme bunu fark etsin. İlişkiler servis tarafından
 * bağlanır.
 */
@Mapper(config = MapperYapilandirmasi.class)
public interface SarfMalzemesiMapper {

    SarfMalzemesiResponse cevap(Envanter envanter);

    @SarftaKullanilmayanAlanlar
    Envanter yeni(CreateSarfMalzemesiRequest istek);

    @SarftaKullanilmayanAlanlar
    void guncelle(UpdateSarfMalzemesiRequest istek, @MappingTarget Envanter envanter);

    @ObjectFactory
    default Envanter yeniSarf(CreateSarfMalzemesiRequest istek) {
        return Envanter.sarf(istek.ad());
    }

    /** Demirbaşa özgü alanlar ve servisin ayrıca bağladığı ilişkiler. */
    @Retention(RetentionPolicy.CLASS)
    @Mapping(target = "demirbasNo", ignore = true)
    @Mapping(target = "seriNo", ignore = true)
    @Mapping(target = "birimKodu", ignore = true)
    @Mapping(target = "alimYolu", ignore = true)
    @Mapping(target = "durum", ignore = true)
    @Mapping(target = "garanti", ignore = true)
    @Mapping(target = "saymanlikNo", ignore = true)
    @Mapping(target = "zimmetliSicil", ignore = true)
    @Mapping(target = "ilSubeDaireBskNo", ignore = true)
    @Mapping(target = "alimTuruKodu", ignore = true)
    @Mapping(target = "kategori", ignore = true)
    @Mapping(target = "marka", ignore = true)
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "depo", ignore = true)
    @Mapping(target = "ihale", ignore = true)
    @interface SarftaKullanilmayanAlanlar {
    }
}
