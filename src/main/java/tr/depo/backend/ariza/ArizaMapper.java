package tr.depo.backend.ariza;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.ariza.dto.ArizaResponse;
import tr.depo.backend.ariza.dto.CreateArizaRequest;
import tr.depo.backend.ariza.dto.UpdateArizaRequest;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.domain.ariza.Ariza;
import tr.depo.domain.ortak.Sicil;

/** Arıza ↔ DTO. Envanter ve depo servis tarafından bağlanır. */
@Mapper(config = MapperYapilandirmasi.class)
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
