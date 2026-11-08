package tr.depo.backend.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import tr.depo.backend.config.MapStructConfig;
import tr.depo.backend.dto.CreateModelRequest;
import tr.depo.backend.dto.ModelResponse;
import tr.depo.backend.dto.UpdateModelRequest;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/**
 * Model ↔ DTO. Sarf modelinin markası domain fabrikasında zorunlu olduğundan servis tarafından çözülüp
 * {@code @Context} ile verilir; diğer ilişkiler servis tarafından bağlanır.
 */
@Mapper(config = MapStructConfig.class)
public interface ModelMapper {

    ModelResponse cevap(Model model);

    @Mapping(target = "marka", ignore = true)
    @Mapping(target = "kategori", ignore = true)
    @Mapping(target = "depo", ignore = true)
    @Mapping(target = "aktif", conditionExpression = "java(istek.aktif() != null)")
    Model yeni(CreateModelRequest istek, @Context Marka marka);

    @Mapping(target = "marka", ignore = true)
    @Mapping(target = "kategori", ignore = true)
    @Mapping(target = "depo", ignore = true)
    void guncelle(UpdateModelRequest istek, @MappingTarget Model model);

    /** Sarf modelinde {@code aktif} boş bırakılırsa {@code true} (Marka ile aynı davranış). */
    @ObjectFactory
    default Model yeniModel(CreateModelRequest istek, @Context Marka marka) {
        return istek.tur() == MalzemeTuru.SARF
            ? Model.sarfModeli(marka, istek.ad(), istek.aktif() == null || istek.aktif())
            : Model.demirbasModeli(istek.ad());
    }
}
