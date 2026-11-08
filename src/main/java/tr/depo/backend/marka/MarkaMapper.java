package tr.depo.backend.marka;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.common.mapping.MapperYapilandirmasi;
import tr.depo.backend.marka.dto.CreateMarkaRequest;
import tr.depo.backend.marka.dto.MarkaResponse;
import tr.depo.backend.marka.dto.UpdateMarkaRequest;
import tr.depo.domain.tanim.Marka;

/**
 * Marka ↔ DTO. Entity, domain'in kendi kurucusuyla oluşturulur ({@link #yeniMarka}); böylece türe özgü varsayılanlar
 * (sarfta {@code aktif = true}) korunur. İlişkiler (depo) ID'den servis tarafından çözüldüğü için burada eşlenmez.
 */
@Mapper(config = MapperYapilandirmasi.class)
public interface MarkaMapper {

    MarkaResponse cevap(Marka marka);

    @Mapping(target = "depo", ignore = true)
    @Mapping(target = "aktif", conditionExpression = "java(istek.aktif() != null)")
    Marka yeni(CreateMarkaRequest istek);

    @Mapping(target = "depo", ignore = true)
    void guncelle(UpdateMarkaRequest istek, @MappingTarget Marka marka);

    @ObjectFactory
    default Marka yeniMarka(CreateMarkaRequest istek) {
        return new Marka(istek.tur(), istek.ad());
    }
}
