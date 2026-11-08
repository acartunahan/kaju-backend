package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.CreateEnvanterNotuRequest;
import tr.depo.backend.dto.EnvanterNotuResponse;
import tr.depo.backend.dto.UpdateEnvanterNotuRequest;
import tr.depo.domain.envanter.EnvanterNotu;

/** Not ↔ DTO. Envanter servis tarafından bağlanır. */
@Mapper(config = MapStructConfig.class)
public interface EnvanterNotuMapper {

    @Mapping(target = "envanterId", source = "envanter.id")
    EnvanterNotuResponse cevap(EnvanterNotu not);

    @Mapping(target = "envanter", ignore = true)
    EnvanterNotu yeni(CreateEnvanterNotuRequest istek);

    @Mapping(target = "envanter", ignore = true)
    void guncelle(UpdateEnvanterNotuRequest istek, @MappingTarget EnvanterNotu not);
}
