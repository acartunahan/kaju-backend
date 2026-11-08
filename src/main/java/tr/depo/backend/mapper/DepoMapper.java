package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.CreateDepoRequest;
import tr.depo.backend.dto.DepoResponse;
import tr.depo.backend.dto.UpdateDepoRequest;
import tr.depo.domain.tanim.Depo;

@Mapper(config = MapStructConfig.class)
public interface DepoMapper {

    DepoResponse cevap(Depo depo);

    Depo yeni(CreateDepoRequest istek);

    void guncelle(UpdateDepoRequest istek, @MappingTarget Depo depo);

    @ObjectFactory
    default Depo yeniDepo(CreateDepoRequest istek) {
        return new Depo(istek.ad());
    }
}
