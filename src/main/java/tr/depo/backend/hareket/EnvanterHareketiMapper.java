package tr.depo.backend.hareket;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.hareket.dto.CreateEnvanterHareketiRequest;
import tr.depo.backend.hareket.dto.EnvanterHareketiResponse;
import tr.depo.domain.envanter.EnvanterHareketi;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Hareket ↔ DTO. Hareket türe göre domain fabrikasıyla oluşur; ilişkiler (envanter, depolar) servis tarafından
 * bağlanır. Legacy eski referanslar yeni kayıtlarda yazılmaz.
 */
@Mapper(config = MapperYapilandirmasi.class)
public interface EnvanterHareketiMapper {

    EnvanterHareketiResponse cevap(EnvanterHareketi hareket);

    @Mapping(target = "envanter", ignore = true)
    @Mapping(target = "neredenDepo", ignore = true)
    @Mapping(target = "nereyeDepo", ignore = true)
    @Mapping(target = "alanDepo", ignore = true)
    @Mapping(target = "markaEskiRef", ignore = true)
    @Mapping(target = "modelEskiRef", ignore = true)
    @Mapping(target = "firmaEskiRef", ignore = true)
    EnvanterHareketi yeni(CreateEnvanterHareketiRequest istek);

    @ObjectFactory
    default EnvanterHareketi yeniHareket(CreateEnvanterHareketiRequest istek) {
        return istek.tur() == MalzemeTuru.DEMIRBAS
            ? EnvanterHareketi.demirbasHareketi(istek.hareketNoKodu())
            : EnvanterHareketi.sarfHareketi();
    }
}
