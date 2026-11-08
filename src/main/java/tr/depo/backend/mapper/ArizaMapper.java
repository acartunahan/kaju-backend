package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.ArizaResponse;
import tr.depo.backend.dto.CreateArizaRequest;
import tr.depo.backend.dto.UpdateArizaRequest;
import tr.depo.domain.ariza.Ariza;
import tr.depo.domain.ortak.Sicil;

/** Arıza ↔ DTO. Envanter ve depo servis tarafından bağlanır. */
@Mapper(config = MapStructConfig.class)
public interface ArizaMapper {

    ArizaResponse cevap(Ariza ariza);

    @Mapping(target = "envanter", ignore = true)
    @Mapping(target = "depo", ignore = true)
    Ariza yeni(CreateArizaRequest istek);

    @Mapping(target = "envanter", ignore = true)
    @Mapping(target = "depo", ignore = true)
    void guncelle(UpdateArizaRequest istek, @MappingTarget Ariza ariza);

    @ObjectFactory
    default Ariza yeniAriza(CreateArizaRequest istek) {
        return new Ariza(new Sicil(istek.bildirenSicil()), istek.sebep(), istek.asamaKodu(), istek.tarih());
    }
}
