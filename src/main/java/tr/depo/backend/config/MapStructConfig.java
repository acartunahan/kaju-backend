package tr.depo.backend.config;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import tr.depo.backend.mapper.DegerNesnesiMapper;
import tr.depo.backend.mapper.IliskiOzetiMapper;

/**
 * Tüm MapStruct mapper'larının ortak ayarı: Spring bean'i, constructor injection (alan enjeksiyonu yasak) ve
 * eşlenmemiş hedef alan derleme hatası (yeni bir alan eklenip DTO'ya yansıtılmazsa build kırılır).
 */
@MapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = {IliskiOzetiMapper.class, DegerNesnesiMapper.class})
public interface MapStructConfig {
}
