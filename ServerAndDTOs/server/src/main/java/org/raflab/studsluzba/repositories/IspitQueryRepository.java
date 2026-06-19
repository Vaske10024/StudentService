package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IspitQueryRepository extends CrudRepository<PrijavaIspita, Long> {

    @Query("select si from PrijavaIspita pi " +
            "join pi.student si " +
            "left join fetch si.student " +
            "where pi.ispit.id = :ispitId and pi.ponisteno = false")
    List<StudentIndeks> prijavljeniZaIspit(@Param("ispitId") Long ispitId);

    @Query("select avg(pi.ocena) from PrijavaIspita pi " +
            "where pi.ispit.id = :ispitId and pi.daLiJeIzasao = true and pi.ponisteno = false and pi.ocena > 0")
    Double prosecnaOcenaNaIspitu(@Param("ispitId") Long ispitId);

    @Query("select count(pi) from PrijavaIspita pi " +
            "where pi.student.id = :studentIndeksId " +
            "and (pi.predmet.id = :predmetId or pi.ispit.drziPredmet.predmet.id = :predmetId) " +
            "and pi.ponisteno = false " +
            "and pi.daLiJeIzasao = true")
    Long brojPolaganja(@Param("studentIndeksId") Long studentIndeksId,
                       @Param("predmetId") Long predmetId);

    @Query("select pi from PrijavaIspita pi " +
            "where pi.student.studProgramOznaka = :sp and pi.student.godina = :godina and pi.student.broj = :broj " +
            "and pi.daLiJeIzasao = true and pi.ponisteno = false and pi.ocena >= 6")
    List<PrijavaIspita> polozeniByIndex(@Param("sp") String sp,
                                        @Param("godina") int godina,
                                        @Param("broj") int broj);

    @Query("select pi from PrijavaIspita pi " +
            "where pi.ispit.id = :ispitId and pi.ponisteno = false " +
            "order by pi.student.studProgramOznaka, pi.student.godina, pi.student.broj")
    List<PrijavaIspita> rezultatiSortirani(@Param("ispitId") Long ispitId);

    @Query("select avg(pi.ocena) from PrijavaIspita pi " +
            "where pi.ispit.drziPredmet.predmet.id = :predmetId and pi.ocena >= 6 " +
            "and pi.student.godina between :fromYear and :toYear")
    Double prosecnaOcenaZaPredmetURasponu(@Param("predmetId") Long predmetId,
                                          @Param("fromYear") int fromYear,
                                          @Param("toYear") int toYear);

    @Query("select pi from PrijavaIspita pi " +
            "where pi.ispit.id = :ispitId and pi.student.id = :studentIndeksId and pi.ponisteno = false " +
            "and pi.status = org.raflab.studsluzba.model.ispiti.PrijavaStatus.PRIJAVLJEN")
    Optional<PrijavaIspita> findAktivnaPrijava(@Param("ispitId") Long ispitId,
                                               @Param("studentIndeksId") Long studentIndeksId);

    @Query("select pi from PrijavaIspita pi where pi.ispit.id = :ispitId and pi.ponisteno = false")
    List<PrijavaIspita> aktivnePrijaveZaIspit(@Param("ispitId") Long ispitId);

    @Query("select pi from PrijavaIspita pi " +
            "where pi.student.id = :studentIndeksId and pi.ponisteno = false and pi.status = org.raflab.studsluzba.model.ispiti.PrijavaStatus.PRIJAVLJEN and pi.daLiJeIzasao = false and pi.ispit is not null " +
            "order by pi.ispit.datumOdrzavanja asc")
    List<PrijavaIspita> activeRegistrationsForStudent(@Param("studentIndeksId") Long studentIndeksId);

    @Query("select pi from PrijavaIspita pi " +
            "where pi.student.id = :studentIndeksId and (pi.daLiJeIzasao = true or pi.ponisteno = true " +
            "or pi.priznatSDrugogFakulteta = true or pi.status = org.raflab.studsluzba.model.ispiti.PrijavaStatus.ODJAVLJEN) " +
            "order by pi.datumPrijave desc")
    List<PrijavaIspita> previousAttemptsForStudent(@Param("studentIndeksId") Long studentIndeksId);

    @Query("select pi from PrijavaIspita pi " +
            "where pi.student.studProgramOznaka = :sp and pi.student.godina = :godina and pi.student.broj = :broj " +
            "and pi.priznatSDrugogFakulteta = true and pi.ocena >= 6")
    List<PrijavaIspita> priznatiByIndex(@Param("sp") String sp,
                                        @Param("godina") int godina,
                                        @Param("broj") int broj);

    @Query("select case when count(pi)>0 then true else false end from PrijavaIspita pi " +
            "where pi.student.id = :studentIndeksId " +
            "and (pi.predmet.id = :predmetId or pi.ispit.drziPredmet.predmet.id = :predmetId) " +
            "and pi.ponisteno = false and pi.ocena >= 6 " +
            "and (pi.daLiJeIzasao = true or pi.priznatSDrugogFakulteta = true)")
    boolean existsPassedSubject(@Param("studentIndeksId") Long studentIndeksId,
                                @Param("predmetId") Long predmetId);

    @Query("select case when count(pi)>0 then true else false end from PrijavaIspita pi " +
            "where pi.student.id = :studentIndeksId and pi.predmet.id = :predmetId " +
            "and pi.priznatSDrugogFakulteta = true and pi.ocena >= 6 and pi.ponisteno = false")
    boolean existsRecognizedSubject(@Param("studentIndeksId") Long studentIndeksId,
                                    @Param("predmetId") Long predmetId);

    @Query("select avg(pi.ocena) from PrijavaIspita pi where pi.student.id = :studentIndeksId " +
            "and pi.ponisteno = false and pi.ocena between 6 and 10 " +
            "and (pi.daLiJeIzasao = true or pi.priznatSDrugogFakulteta = true)")
    Double averagePassedGrade(@Param("studentIndeksId") Long studentIndeksId);

    @Query("select pi from PrijavaIspita pi where pi.student.id = :studentIndeksId "
            + "and pi.ponisteno = false and pi.ocena between 6 and 10 "
            + "and (pi.daLiJeIzasao = true or pi.priznatSDrugogFakulteta = true)")
    List<PrijavaIspita> passedAttemptsForStudent(@Param("studentIndeksId") Long studentIndeksId);

    @Query("select pi from PrijavaIspita pi where pi.student.id = :studentIndeksId "
            + "and pi.ponisteno = false "
            + "and (pi.predmet.id = :predmetId or pi.ispit.predmet.id = :predmetId or pi.ispit.drziPredmet.predmet.id = :predmetId) "
            + "order by pi.datumPrijave desc, pi.id desc")
    List<PrijavaIspita> attemptsForStudentSubject(@Param("studentIndeksId") Long studentIndeksId,
                                                  @Param("predmetId") Long predmetId);

    List<PrijavaIspita> findByIspitId(Long ispitId);
}
