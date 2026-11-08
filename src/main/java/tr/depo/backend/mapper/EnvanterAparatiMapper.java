package tr.depo.backend.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.CreateEnvanterAparatiRequest;
import tr.depo.backend.dto.EnvanterAparatiResponse;
import tr.depo.backend.dto.UpdateEnvanterAparatiRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterAparati;

/** Aparat ↔ DTO. Envanter domain kurucusunda zorunlu olduğundan {@code @Context} ile verilir. */
@Mapper(config = MapStructConfig.class)
public interface EnvanterAparatiMapper {

    @Mapping(target = "envanterId", source = "envanter.id")
    EnvanterAparatiResponse cevap(EnvanterAparati aparat);

    @Mapping(target = "envanter", ignore = true)
    EnvanterAparati yeni(CreateEnvanterAparatiRequest istek, @Context Envanter envanter);

    @Mapping(target = "envanter", ignore = true)
    void guncelle(UpdateEnvanterAparatiRequest istek, @MappingTarget EnvanterAparati aparat);

    @ObjectFactory
    default EnvanterAparati yeniAparat(CreateEnvanterAparatiRequest istek, @Context Envanter envanter) {
        return new EnvanterAparati(envanter, istek.ad(), istek.adet());
    }
}
