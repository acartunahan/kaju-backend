package tr.depo.backend.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class OracleHataCeviriciTest {

    private final OracleHataCevirici cevirici = new OracleHataCevirici();

    private static DataIntegrityViolationException oracleHatasi(int kod, String mesaj) {
        return new DataIntegrityViolationException("hata", new RuntimeException("sarmalayan", new SQLException(mesaj, "23000", kod)));
    }

    @Test
    void shouldTranslateUniqueViolationWithConstraintName() {
        assertThat(cevirici.cevir(oracleHatasi(1, "ORA-00001: unique constraint (HEDEF.UK_ENVANTER_FOTOGRAFI_NESNE) violated")))
            .get()
            .isInstanceOfSatisfying(TekillikIhlaliException.class, hata -> {
                assertThat(hata.kisit()).isEqualTo("UK_ENVANTER_FOTOGRAFI_NESNE");
                assertThat(hata.mesajAnahtari()).isEqualTo("kisit.UK_ENVANTER_FOTOGRAFI_NESNE");
                assertThat(hata.hataKodu()).isEqualTo(HataKodu.TEKILLIK_IHLALI);
            });
    }

    @Test
    void shouldTranslateCheckAndMissingParentToDataRuleViolation() {
        assertThat(cevirici.cevir(oracleHatasi(2290, "ORA-02290: check constraint (HEDEF.CK_MARKA_SARF_AKTIF) violated")))
            .get().isInstanceOfSatisfying(VeriKuraliException.class,
                hata -> assertThat(hata.kisit()).isEqualTo("CK_MARKA_SARF_AKTIF"));
        assertThat(cevirici.cevir(oracleHatasi(2291, "ORA-02291: integrity constraint (HEDEF.FK_MODEL_MARKA) violated - parent key not found")))
            .get().isInstanceOfSatisfying(VeriKuraliException.class,
                hata -> assertThat(hata.hataKodu()).isEqualTo(HataKodu.VERI_KURALI_IHLALI));
    }

    @Test
    void shouldNotTranslateChildRecordOrPhysicalDeleteErrors() {
        assertThat(cevirici.cevir(oracleHatasi(2292, "ORA-02292: integrity constraint (HEDEF.FK_X) violated - child record found"))).isEmpty();
        assertThat(cevirici.cevir(oracleHatasi(20001, "ORA-20001: Fiziksel silme yasak: MARKA"))).isEmpty();
    }

    @Test
    void shouldNotTranslateNonSqlErrors() {
        assertThat(cevirici.cevir(new DataIntegrityViolationException("sql yok"))).isEmpty();
    }

    @Test
    void shouldReadConstraintNameWithOrWithoutSchemaAndQuotes() {
        assertThat(OracleHataCevirici.kisitAdi("ORA-00001: unique constraint (UK_A) violated")).isEqualTo("UK_A");
        assertThat(OracleHataCevirici.kisitAdi("ORA-00001: unique constraint (\"HEDEF\".\"UK_B\") violated")).isEqualTo("UK_B");
        assertThat(OracleHataCevirici.kisitAdi("anlamsız mesaj")).isEqualTo("BILINMIYOR");
        assertThat(OracleHataCevirici.kisitAdi(null)).isEqualTo("BILINMIYOR");
    }
}
