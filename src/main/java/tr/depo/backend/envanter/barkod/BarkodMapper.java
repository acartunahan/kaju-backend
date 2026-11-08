package tr.depo.backend.envanter.barkod;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.envanter.barkod.dto.BarkodResponse;
import tr.depo.backend.envanter.barkod.dto.CreateBarkodRequest;
import tr.depo.backend.envanter.barkod.dto.UpdateBarkodRequest;
import tr.depo.domain.envanter.Barkod;

/** Barkod ↔ DTO. Envanter servis tarafından bağlanır. */
@Mapper(config = MapperYapilandirmasi.class)
public interface BarkodMapper {

    @Mapping(target = "envanterId", source = "envanter.id")
    BarkodResponse cevap(Barkod barkod);

    @Mapping(target = "envanter", ignore = true)
    Barkod yeni(CreateBarkodRequest istek);

    @Mapping(target = "envanter", ignore = true)
    void guncelle(UpdateBarkodRequest istek, @MappingTarget Barkod barkod);

    @ObjectFactory
    default Barkod yeniBarkod(CreateBarkodRequest istek) {
        return new Barkod(istek.barkod(), istek.seriNoMu());
    }
}
