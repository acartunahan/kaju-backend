package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.ZimmetGecmisiResponse;
import tr.depo.domain.envanter.ZimmetGecmisi;

/** Zimmet geçmişi yalnızca okunur; satırları servis domain fabrikasıyla oluşturur. */
@Mapper(config = MapStructConfig.class)
public interface ZimmetGecmisiMapper {

    @Mapping(target = "demirbasId", source = "envanter.id")
    ZimmetGecmisiResponse cevap(ZimmetGecmisi kayit);
}
