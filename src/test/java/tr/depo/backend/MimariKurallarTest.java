package tr.depo.backend;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.EntityManager;
import java.util.Locale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.bind.annotation.RestController;

/**
 * Yapısal kurallar build'de zorlanır. En önemlisi fiziksel silmenin uygulama kodunda imkânsız olmasıdır:
 * repository'ler {@code delete*} açan arayüzlerden türeyemez, {@code EntityManager.remove} çağrılamaz,
 * {@code DELETE} içeren sorgu yazılamaz (domain'de ayrıca {@code @PreRemove} ve veritabanı trigger'ları var).
 */
@AnalyzeClasses(packages = "tr.depo.backend", importOptions = ImportOption.DoNotIncludeTests.class)
class MimariKurallarTest {

    @ArchTest
    static final ArchRule repositoryDeleteMetotlariAcmaz = noClasses()
        .should().beAssignableTo(CrudRepository.class)
        .orShould().beAssignableTo(ListCrudRepository.class)
        .orShould().beAssignableTo(JpaRepository.class)
        .orShould().beAssignableTo(ReactiveCrudRepository.class)
        .because("fiziksel silme yasak; repository'ler AktifKayitRepository'den türemeli (delete* metodu yok)");

    @ArchTest
    static final ArchRule entityManagerRemoveCagrilmaz = noClasses()
        .should().callMethod(EntityManager.class, "remove", Object.class)
        .because("fiziksel silme yasak; silme entity.sil(silen) ile yapılır");

    @ArchTest
    static final ArchRule sorgularDeleteIcermez = methods()
        .that().areAnnotatedWith(Query.class)
        .should(deleteIcermeyenSorgu())
        .allowEmptyShould(true)
        .because("fiziksel silme yasak; native ya da JPQL DELETE yazılmaz");

    @ArchTest
    static final ArchRule controllerEntityGormez = noClasses()
        .that().areAnnotatedWith(RestController.class)
        .should().dependOnClassesThat().resideInAPackage("tr.depo.domain..")
        .allowEmptyShould(true)
        .because("entity'ler API katmanına sızmaz; controller yalnızca DTO ile çalışır");

    @ArchTest
    static final ArchRule controllerImplVeRepositoryyeBaglanmaz = noClasses()
        .that().areAnnotatedWith(RestController.class)
        .should().dependOnClassesThat().haveSimpleNameEndingWith("ServiceImpl")
        .orShould().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
        .allowEmptyShould(true)
        .because("controller XService arayüzüne bağımlıdır (Dependency Inversion)");

    @ArchTest
    static final ArchRule controllerAdlandirmasi = classes()
        .that().areAnnotatedWith(RestController.class)
        .should().haveSimpleNameEndingWith("Controller")
        .allowEmptyShould(true);

    @ArchTest
    static final ArchRule alanEnjeksiyonuYok = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    private static ArchCondition<JavaMethod> deleteIcermeyenSorgu() {
        return new ArchCondition<>("DELETE içermeyen sorgu") {
            @Override
            public void check(JavaMethod metot, ConditionEvents olaylar) {
                String sorgu = metot.getAnnotationOfType(Query.class).value().toLowerCase(Locale.ROOT);
                boolean deleteVar = sorgu.matches("(?s).*\\bdelete\\b.*");
                olaylar.add(new SimpleConditionEvent(metot, !deleteVar,
                    metot.getFullName() + " sorgusu DELETE içeriyor"));
            }
        };
    }
}
