package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.CreateKategoriRequest;
import tr.depo.backend.dto.KategoriResponse;
import tr.depo.backend.dto.UpdateKategoriRequest;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.MalzemeTuru;

/** Kategori ↔ DTO. Üst kategori ve depo ID'den servis tarafından çözülür. */
@Mapper(config = MapStructConfig.class)
public interface KategoriMapper {

    KategoriResponse cevap(Kategori kategori);

    @Mapping(target = "ust", ignore = true)
    @Mapping(target = "depo", ignore = true)
    Kategori yeni(CreateKategoriRequest istek);

    @Mapping(target = "ust", ignore = true)
    @Mapping(target = "depo", ignore = true)
    void guncelle(UpdateKategoriRequest istek, @MappingTarget Kategori kategori);

    @ObjectFactory
    default Kategori yeniKategori(CreateKategoriRequest istek) {
        return istek.tur() == MalzemeTuru.SARF
            ? Kategori.sarfKategorisi(istek.ad())
            : Kategori.demirbasKategorisi(istek.ad(), istek.durumKodu());
    }
}
