package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.CreateEnvanterHareketiRequest;
import tr.depo.backend.dto.EnvanterHareketiResponse;
import tr.depo.domain.envanter.EnvanterHareketi;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Hareket ↔ DTO. Hareket türe göre domain fabrikasıyla oluşur; ilişkiler (envanter, depolar) servis tarafından
 * bağlanır. Legacy eski referanslar yeni kayıtlarda yazılmaz.
 */
@Mapper(config = MapStructConfig.class)
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
