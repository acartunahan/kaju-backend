package tr.depo.backend.common.mapping;

/**
 * Cevaplarda ilişkili kaydın özeti: istemci ek istek atmadan adı gösterebilsin. İlişkili kayıt silinmişse
 * ilişki alanı {@code null} döner (silinmiş kayıtlar ilişkilerde görünmez).
 */
public record IliskiOzeti(Long id, String ad) {
}
