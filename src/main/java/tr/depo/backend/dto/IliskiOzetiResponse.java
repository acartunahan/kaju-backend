package tr.depo.backend.dto;

/**
 * Cevaplarda ilişkili kaydın özeti: istemci ek istek atmadan adı gösterebilsin. İlişkili kayıt silinmişse
 * ilişki alanı {@code null} döner (silinmiş kayıtlar ilişkilerde görünmez).
 */
public record IliskiOzetiResponse(Long id, String ad) {
}
