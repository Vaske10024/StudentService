package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentPodaciRepository extends JpaRepository<StudentPodaci, Long> {
    boolean existsByJmbg(String jmbg);
    boolean existsByEmailFakultetskiIgnoreCase(String email);
    boolean existsByEmailPrivatniIgnoreCase(String email);

    /**
     * Vraća SVE StudentPodaci, filtrirano po "contains" ime/prezime,
     * a po indeksu samo ako je neki od (studProgram, godina, broj) prosleđen.
     * Koristimo EXISTS da izbegnemo duplikate zbog join-a.
     */
    @Query("select sp from StudentPodaci sp " +
            "where (:ime is null or lower(sp.ime) like lower(concat('%', :ime, '%'))) " +
            "and (:prezime is null or lower(sp.prezime) like lower(concat('%', :prezime, '%'))) " +
            "and ((:studProgram is null and :godina is null and :broj is null) " +
            "or exists ( " +
            "   select 1 from StudentIndeks si " +
            "   where si.student = sp " +
            "   and (:studProgram is null or lower(si.studProgramOznaka) like lower(concat('%', :studProgram, '%'))) " +
            "   and (:godina is null or si.godina = :godina) " +
            "   and (:broj is null or si.broj = :broj) " +
            "))")
    Page<StudentPodaci> searchAll(@Param("ime") String ime,
                                  @Param("prezime") String prezime,
                                  @Param("studProgram") String studProgram,
                                  @Param("godina") Integer godina,
                                  @Param("broj") Integer broj,
                                  Pageable pageable);

    @Query("select sp from StudentPodaci sp where " +
            "lower(concat(concat(sp.ime, ' '), sp.prezime)) like lower(concat('%', :q, '%')) " +
            "or lower(concat(concat(sp.prezime, ' '), sp.ime)) like lower(concat('%', :q, '%')) " +
            "or lower(sp.emailFakultetski) like lower(concat('%', :q, '%')) " +
            "or lower(sp.emailPrivatni) like lower(concat('%', :q, '%')) " +
            "or exists (select si.id from StudentIndeks si where si.student = sp and lower(si.studProgramOznaka) like lower(concat('%', :q, '%')))")
    Page<StudentPodaci> globalSearch(@Param("q") String q, Pageable pageable);

    // (ostavi postojeće ako ti negde još trebaju)

    @Query("select sp from StudentPodaci sp where "
            + "(:ime is null or lower(sp.ime) like :ime) and "
            + "(:prezime is null or lower(sp.prezime) like :prezime) and "
            + "not exists (select indeks from StudentIndeks indeks where indeks.student = sp)")
    Page<StudentPodaci> findStudent(String ime, String prezime, Pageable pageable);

    @Query("select si from StudentIndeks si where si.aktivan = true and si.student.id = :studPodaciId")
    List<StudentIndeks> getAktivanIndeks(@Param("studPodaciId") Long studPodaciId);

    @Query("select si from StudentIndeks si where si.aktivan=false and si.student.id = :studPodaciId")
    List<StudentIndeks> getNeaktivniIndeksi(Long studPodaciId);

    @Query("select sp from StudentPodaci sp where sp.srednjaSkola.naziv = :naziv")
    List<StudentPodaci> findBySrednjaSkola(@Param("naziv") String naziv);
}
