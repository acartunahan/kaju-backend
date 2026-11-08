package tr.depo.backend.exception;

/** İstenen kayıt yok ya da soft delete ile silinmiş (404). Silinmiş kayıt dışarıya "yok" olarak görünür. */
public class KayitBulunamadiException extends KajuException {

    public KayitBulunamadiException(String kaynak, Object id) {
        super(HataKodu.KAYIT_BULUNAMADI, HataKodu.KAYIT_BULUNAMADI.mesajAnahtari(),
            kaynak + " bulunamadı ya da silinmiş: id=" + id, kaynak, id);
    }
}
