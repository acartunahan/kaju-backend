package tr.depo.backend.envanter.not;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.envanter.not.dto.CreateEnvanterNotuRequest;
import tr.depo.backend.envanter.not.dto.EnvanterNotuResponse;
import tr.depo.backend.envanter.not.dto.UpdateEnvanterNotuRequest;
import tr.depo.domain.envanter.EnvanterNotu;

/** Not ↔ DTO. Envanter servis tarafından bağlanır. */
@Mapper(config = MapperYapilandirmasi.class)
public interface EnvanterNotuMapper {

    @Mapping(target = "envanterId", source = "envanter.id")
    EnvanterNotuResponse cevap(EnvanterNotu not);

    @Mapping(target = "envanter", ignore = true)
    EnvanterNotu yeni(CreateEnvanterNotuRequest istek);

    @Mapping(target = "envanter", ignore = true)
    void guncelle(UpdateEnvanterNotuRequest istek, @MappingTarget EnvanterNotu not);
}
