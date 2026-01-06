package com.ntp.taskmanager;

/**
 * Tamamlanabilir (completable) davranışı tanımlayan arayüzdür.
 *
 * <p>
 * Bu arayüzü uygulayan sınıflar,
 * tamamlanma durumunu kontrol edebilir ve güncelleyebilir.
 * </p>
 *
 * <p>
 * OOP kapsamında Interface kullanımına örnektir.
 * </p>
 */
public interface Completable {

    /**
     * Nesneyi tamamlanmış olarak işaretler.
     */
    void complete();

    /**
     * Nesnenin tamamlanıp tamamlanmadığını döndürür.
     *
     * @return true ise tamamlanmıştır, false ise tamamlanmamıştır
     */
    boolean isCompleted();
}
