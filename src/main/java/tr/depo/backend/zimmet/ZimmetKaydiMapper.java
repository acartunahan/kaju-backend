package tr.depo.backend.zimmet;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.zimmet.dto.CreateZimmetKaydiRequest;
import tr.depo.backend.zimmet.dto.ZimmetKaydiResponse;
import tr.depo.domain.envanter.ZimmetKaydi;
import tr.depo.domain.ortak.Sicil;

/** Zimmet kaydı ↔ DTO. Depo servis tarafından bağlanır; legacy eski referans yeni kayıtlarda yazılmaz. */
@Mapper(config = MapperYapilandirmasi.class)
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
