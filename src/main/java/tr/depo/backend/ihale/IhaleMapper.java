package tr.depo.backend.ihale;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.ihale.dto.CreateIhaleRequest;
import tr.depo.backend.ihale.dto.IhaleResponse;
import tr.depo.backend.ihale.dto.UpdateIhaleRequest;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.IhaleKomisyonu;

/** İhale ↔ DTO. Komisyon API'de üç düz alandır; üçü de boşsa komisyon yok sayılır. Firma servis tarafından bağlanır. */
@Mapper(config = MapperYapilandirmasi.class)
public interface IhaleMapper {

    @Mapping(target = "komisyonBaskani", source = "komisyon.baskan")
    @Mapping(target = "komisyonUye1", source = "komisyon.uye1")
    @Mapping(target = "komisyonUye2", source = "komisyon.uye2")
    IhaleResponse cevap(Ihale ihale);

    @Mapping(target = "firma", ignore = true)
    @Mapping(target = "komisyon", ignore = true)
    Ihale yeni(CreateIhaleRequest istek);

    @Mapping(target = "firma", ignore = true)
    @Mapping(target = "komisyon", ignore = true)
    void guncelle(UpdateIhaleRequest istek, @MappingTarget Ihale ihale);

    @ObjectFactory
    default Ihale yeniIhale(CreateIhaleRequest istek) {
        return new Ihale(istek.durumKodu(), istek.kayitTarihi());
    }

    @AfterMapping
    default void komisyon(CreateIhaleRequest istek, @MappingTarget Ihale ihale) {
        ihale.setKomisyon(komisyon(istek.komisyonBaskani(), istek.komisyonUye1(), istek.komisyonUye2()));
    }

    @AfterMapping
    default void komisyon(UpdateIhaleRequest istek, @MappingTarget Ihale ihale) {
        ihale.setKomisyon(komisyon(istek.komisyonBaskani(), istek.komisyonUye1(), istek.komisyonUye2()));
    }

    private static IhaleKomisyonu komisyon(String baskan, String uye1, String uye2) {
        return baskan == null && uye1 == null && uye2 == null ? null : new IhaleKomisyonu(baskan, uye1, uye2);
    }
}
