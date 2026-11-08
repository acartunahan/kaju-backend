package tr.depo.backend.exception;

/** Doğrulama hatası cevabındaki tek bir alanın hatası. Nesne düzeyindeki hatalarda {@code alan} nesne adıdır. */
public record AlanHatasi(String alan, String mesaj) {
}
