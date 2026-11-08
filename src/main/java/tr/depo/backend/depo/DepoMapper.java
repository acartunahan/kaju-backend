package tr.depo.backend.depo;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.depo.dto.CreateDepoRequest;
import tr.depo.backend.depo.dto.DepoResponse;
import tr.depo.backend.depo.dto.UpdateDepoRequest;
import tr.depo.domain.tanim.Depo;

@Mapper(config = MapperYapilandirmasi.class)
public interface DepoMapper {

    DepoResponse cevap(Depo depo);

    Depo yeni(CreateDepoRequest istek);

    void guncelle(UpdateDepoRequest istek, @MappingTarget Depo depo);

    @ObjectFactory
    default Depo yeniDepo(CreateDepoRequest istek) {
        return new Depo(istek.ad());
    }
}
