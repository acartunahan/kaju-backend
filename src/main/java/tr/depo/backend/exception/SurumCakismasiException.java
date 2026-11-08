package tr.depo.backend.exception;

/** Optimistic locking: istemcinin gönderdiği sürüm güncel değil; kayıt başkası tarafından değiştirilmiş (409). */
public class SurumCakismasiException extends CakismaException {

    public SurumCakismasiException(String kaynak, Object id) {
        super(HataKodu.SURUM_CAKISMASI, HataKodu.SURUM_CAKISMASI.mesajAnahtari(),
            kaynak + " başka bir işlemle değiştirilmiş: id=" + id, kaynak, id);
    }
}
