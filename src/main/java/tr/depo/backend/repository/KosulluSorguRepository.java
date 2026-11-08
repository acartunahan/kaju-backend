package tr.depo.backend.repository;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import tr.depo.domain.ortak.BaseEntity;

/**
 * İsteğe bağlı, birleştirilebilir filtrelerle sayfalı liste sorguları (Criteria API). Yalnızca okur: silme ya da
 * güncelleme yapan bir metodu yoktur. Bilerek {@code JpaSpecificationExecutor} kullanılmaz, çünkü o arayüz
 * {@code delete(Specification)} açar (fiziksel silme yasak).
 *
 * <p>Silinmiş kayıtlar her sorguda açık koşulla ({@code silinmeZamani is null}) dışarıda bırakılır; Hibernate'in
 * {@code silinmemis} filtresine ek güvence.
 */
@Repository
public class KosulluSorguRepository {

    /** Filtre koşulları; boş liste "koşul yok" demektir. */
    @FunctionalInterface
    public interface Kosul<T> {
        List<Predicate> uret(CriteriaBuilder cb, Root<T> kok, CriteriaQuery<?> sorgu);
    }

    private static final String YUKLEME_GRAFI = "jakarta.persistence.loadgraph";
    /** LIKE kaçış karakteri. Ters eğik çizgi değil: HQL dize sabitinde {@code '\'} tek karakter olarak okunmuyor. */
    private static final char KACIS = '!';

    private final EntityManager entityManager;

    public KosulluSorguRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * @param birlikteOkunanlar cevapta özetlenen to-one ilişkiler (N+1 olmasın diye aynı sorguda okunur)
     */
    public <T extends BaseEntity> Page<T> sayfa(Class<T> tip, Kosul<T> kosul, Pageable sayfa,
                                                String... birlikteOkunanlar) {
        TypedQuery<T> sorgu = sorgu(tip, kosul, sayfa.getSort(), birlikteOkunanlar);
        if (sayfa.isPaged()) {
            sorgu.setFirstResult((int) sayfa.getOffset());
            sorgu.setMaxResults(sayfa.getPageSize());
        }
        return PageableExecutionUtils.getPage(sorgu.getResultList(), sayfa, () -> say(tip, kosul));
    }

    /** Sayfasız liste; en fazla {@code enFazla} kayıt döner. */
    public <T extends BaseEntity> List<T> liste(Class<T> tip, Kosul<T> kosul, Sort siralama, int enFazla,
                                                String... birlikteOkunanlar) {
        return sorgu(tip, kosul, siralama, birlikteOkunanlar).setMaxResults(enFazla).getResultList();
    }

    /** Büyük/küçük harf duyarsız "içerir" araması; {@code %} ve {@code _} joker olarak yorumlanmaz. */
    public static Predicate icerir(CriteriaBuilder cb, Expression<String> alan, String metin) {
        return cb.like(cb.lower(alan), cb.lower(cb.literal(icerirDeseni(metin))), KACIS);
    }

    /**
     * JPQL {@code like ... escape '!'} için "içerir" deseni: {@code !}, {@code %} ve {@code _} kaçışlanır. Boş metin
     * için {@code null} (filtre uygulanmaz).
     */
    public static String icerirDeseni(String metin) {
        if (metin == null || metin.isBlank()) {
            return null;
        }
        String kacisli = metin.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + kacisli + "%";
    }

    private <T extends BaseEntity> TypedQuery<T> sorgu(Class<T> tip, Kosul<T> kosul, Sort siralama,
                                                       String... birlikteOkunanlar) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> sorgu = cb.createQuery(tip);
        Root<T> kok = sorgu.from(tip);
        sorgu.select(kok).where(kosullar(cb, kok, sorgu, kosul)).orderBy(siralama(cb, kok, siralama));
        TypedQuery<T> tipli = entityManager.createQuery(sorgu);
        if (birlikteOkunanlar.length > 0) {
            EntityGraph<T> graf = entityManager.createEntityGraph(tip);
            graf.addAttributeNodes(birlikteOkunanlar);
            tipli.setHint(YUKLEME_GRAFI, graf);
        }
        return tipli;
    }

    private <T extends BaseEntity> long say(Class<T> tip, Kosul<T> kosul) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> sorgu = cb.createQuery(Long.class);
        Root<T> kok = sorgu.from(tip);
        sorgu.select(cb.count(kok)).where(kosullar(cb, kok, sorgu, kosul));
        return entityManager.createQuery(sorgu).getSingleResult();
    }

    private static <T> Predicate[] kosullar(CriteriaBuilder cb, Root<T> kok, CriteriaQuery<?> sorgu, Kosul<T> kosul) {
        List<Predicate> hepsi = new ArrayList<>(kosul.uret(cb, kok, sorgu));
        hepsi.add(cb.isNull(kok.get("silinmeZamani")));
        return hepsi.toArray(Predicate[]::new);
    }

    private static List<Order> siralama(CriteriaBuilder cb, Root<?> kok, Sort siralama) {
        List<Order> sira = new ArrayList<>();
        for (Sort.Order o : siralama) {
            Path<?> yol = kok;
            for (String parca : o.getProperty().split("\\.")) {
                yol = yol.get(parca);
            }
            sira.add(o.isAscending() ? cb.asc(yol) : cb.desc(yol));
        }
        return sira;
    }
}
