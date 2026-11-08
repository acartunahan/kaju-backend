package tr.depo.backend.common.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import tr.depo.domain.ortak.BaseEntity;

/**
 * Tüm repository'lerin ortak tabanı. Bilerek {@code CrudRepository}/{@code JpaRepository}'den türemez: fiziksel
 * silme yasak olduğu için {@code delete*} metotları hiç açılmaz (çağrılabilecek bir metot yok; ArchUnit testi de
 * türetmeyi engeller). Silme her zaman entity'nin {@code sil()} metoduyla yapılır.
 *
 * <p><b>Dikkat:</b> domain'deki {@code silinmemis} filtresi ID ile yüklemeye uygulanmaz; {@link #findById} silinmiş
 * kaydı da döndürür (geçmiş ilişkiler bozulmasın diye). Servisler kaydı her zaman
 * {@code SoftDeleteServisSablonu.aktifBul} ile alır. {@link #findAll(Pageable)} ve türetilmiş sorgular filtrelidir.
 */
@NoRepositoryBean
public interface AktifKayitRepository<T extends BaseEntity> extends Repository<T, Long> {

    Optional<T> findById(Long id);

    <S extends T> S save(S kayit);

    Page<T> findAll(Pageable sayfa);
}
