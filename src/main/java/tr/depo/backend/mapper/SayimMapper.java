package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.CreateSayimRequest;
import tr.depo.backend.dto.SayimResponse;
import tr.depo.backend.dto.UpdateSayimRequest;
import tr.depo.domain.sayim.Sayim;

/** Sayım ↔ DTO. Depo servis tarafından bağlanır. */
@Mapper(config = MapStructConfig.class)
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
