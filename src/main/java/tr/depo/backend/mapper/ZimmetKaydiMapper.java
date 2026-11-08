package tr.depo.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.CreateZimmetKaydiRequest;
import tr.depo.backend.dto.ZimmetKaydiResponse;
import tr.depo.domain.envanter.ZimmetKaydi;
import tr.depo.domain.ortak.Sicil;

/** Zimmet kaydı ↔ DTO. Depo servis tarafından bağlanır; legacy eski referans yeni kayıtlarda yazılmaz. */
@Mapper(config = MapStructConfig.class)
public interface ZimmetKaydiMapper {

    ZimmetKaydiResponse cevap(ZimmetKaydi kayit);

    @Mapping(target = "depo", ignore = true)
    @Mapping(target = "malzemeEskiRef", ignore = true)
    ZimmetKaydi yeni(CreateZimmetKaydiRequest istek);

    @ObjectFactory
    default ZimmetKaydi yeniKayit(CreateZimmetKaydiRequest istek) {
        return new ZimmetKaydi(new Sicil(istek.sicil()), istek.islem());
    }
}
