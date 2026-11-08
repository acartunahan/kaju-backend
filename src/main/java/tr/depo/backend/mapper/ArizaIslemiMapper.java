package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.ArizaIslemiResponse;
import tr.depo.backend.dto.CreateArizaIslemiRequest;
import tr.depo.backend.dto.UpdateArizaIslemiRequest;
import tr.depo.domain.ariza.ArizaIslemi;
import tr.depo.domain.ortak.Sicil;

/** Arıza işlemi ↔ DTO. Arıza servis tarafından bağlanır. */
@Mapper(config = MapStructConfig.class)
public interface ArizaIslemiMapper {

    @Mapping(target = "arizaId", source = "ariza.id")
    ArizaIslemiResponse cevap(ArizaIslemi islem);

    @Mapping(target = "ariza", ignore = true)
    ArizaIslemi yeni(CreateArizaIslemiRequest istek);

    @Mapping(target = "ariza", ignore = true)
    void guncelle(UpdateArizaIslemiRequest istek, @MappingTarget ArizaIslemi islem);

    @ObjectFactory
    default ArizaIslemi yeniIslem(CreateArizaIslemiRequest istek) {
        return new ArizaIslemi(istek.islemKodu(), new Sicil(istek.islemYapanSicil()), new Sicil(istek.gorevliSicil()),
            istek.zaman(), istek.miad(), istek.tur());
    }
}
