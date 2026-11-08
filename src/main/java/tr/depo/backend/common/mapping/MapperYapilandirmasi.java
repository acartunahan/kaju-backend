package tr.depo.backend.common.mapping;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Tüm MapStruct mapper'larının ortak ayarı: Spring bean'i, constructor injection (alan enjeksiyonu yasak) ve
 * eşlenmemiş hedef alan derleme hatası (yeni bir alan eklenip DTO'ya yansıtılmazsa build kırılır).
 */
@MapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = {IliskiOzetiEsleyici.class, DegerNesnesiEsleyici.class})
public interface MapperYapilandirmasi {
}
