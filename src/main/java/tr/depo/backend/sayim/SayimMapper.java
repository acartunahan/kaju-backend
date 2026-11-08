package tr.depo.backend.sayim;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.sayim.dto.CreateSayimRequest;
import tr.depo.backend.sayim.dto.SayimResponse;
import tr.depo.backend.sayim.dto.UpdateSayimRequest;
import tr.depo.domain.sayim.Sayim;

/** Sayım ↔ DTO. Depo servis tarafından bağlanır. */
@Mapper(config = MapperYapilandirmasi.class)
public interface SayimMapper {

    SayimResponse cevap(Sayim sayim);

    @Mapping(target = "depo", ignore = true)
    Sayim yeni(CreateSayimRequest istek);

    @Mapping(target = "depo", ignore = true)
    void guncelle(UpdateSayimRequest istek, @MappingTarget Sayim sayim);

    @ObjectFactory
    default Sayim yeniSayim(CreateSayimRequest istek) {
        return new Sayim(istek.kapsam(), istek.baslik());
    }
}
