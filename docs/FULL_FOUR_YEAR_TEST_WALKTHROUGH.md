# Detaljan opis četvorogodišnjeg lifecycle testa

## 1. Šta je ovaj test

Glavni četvorogodišnji test nalazi se u:

`ServerAndDTOs/server/src/test/java/org/raflab/studsluzba/e2e/FullStudyLifecycleIT.java`

Test je Spring Boot integration test. Pokreće stvarne servise, repozitorijume, JPA/Hibernate poslovnu logiku, security naloge i izolovanu H2 bazu.

Ovo nije Playwright test koji sve korake izvršava klikovima u browseru. Playwright proverava frontend rute i stvarnu komunikaciju frontenda sa backendom, dok `FullStudyLifecycleIT` detaljno i deterministički izvršava kompletno četvorogodišnje studiranje.

Svaki scenario je označen sa `@Transactional`, pa se svi podaci automatski poništavaju nakon testa. Test ne zavisi od postojećih podataka u razvojnoj ili produkcionoj bazi.

## 2. Izolacija test podataka

`E2ETestDataFactory` za svako pokretanje generiše jedinstveni prefiks:

`E2E_TEST_<UUID>`

Prefiks se koristi za šifre programa, predmeta, naloge i druge jedinstvene podatke. Zbog toga test može da se ponavlja bez konflikta sa prethodnim pokretanjima ili postojećim podacima.

Test profil svakom Spring test kontekstu dodeljuje i posebnu H2 bazu:

`jdbc:h2:mem:${random.uuid}`

## 3. Akademski dataset

Metoda `E2ETestDataFactory.createAcademicDataset()` kreira:

- vrstu studija za test;
- program `E2E_TEST - Digitalno inzenjerstvo i AI`;
- trajanje programa od 4 godine i 8 semestara;
- ukupno 240 ESPB;
- 5 uzastopnih školskih godina;
- 16 predmeta, po 4 predmeta za svaku godinu;
- svaki predmet vredi 15 ESPB;
- dva semestra unutar svake studijske godine;
- realizaciju svakog predmeta u odgovarajućoj školskoj godini;
- profesora i njegov `PROFESSOR` nalog;
- dodelu profesora kao nosioca svih realizacija;
- poseban `ADMIN` nalog.

Pet školskih godina se kreira zato što sistem prilikom zahteva za upis traži sledeću konfigurisanu školsku godinu. Prve četiri se koriste za studiranje, a peta omogućava ispravnu završnu eligibility logiku.

## 4. Glavni student

Metoda `createStudent(...)` kreira:

- lične podatke studenta;
- aktivan indeks vezan za novi studijski program;
- `STUDENT` nalog;
- privremenu lozinku;
- početni upis prve godine;
- početne `SlusaPredmet` zapise za predmete prve godine.

Test zatim proverava da je nalog nov, da zahteva promenu privremene lozinke i da posle promene više nema `mustChangePassword` oznaku.

## 5. Promena aktivne školske godine

Na početku je aktivna prva školska godina.

Nakon završetka svake studijske godine test:

1. proverava da student ispunjava uslov za sledeću godinu;
2. podnosi zahtev za upis;
3. admin potvrđuje checklistu;
4. admin odobrava zahtev;
5. poziva `SkolskaGodinaService.activate(...)` za sledeću školsku godinu;
6. proverava da student vidi tačno predmete nove godine.

Tok aktivnih godina je:

`2100/2101 -> 2101/2102 -> 2102/2103 -> 2103/2104`

Aktivacija prethodno deaktivira ostale školske godine, tako da u sistemu ostaje samo jedna aktivna godina.

## 6. Prva godina

Glavni scenario prvo proverava da student vidi tačno četiri predmeta prve godine i da nema dupliranih `SlusaPredmet` zapisa.

Zatim se kreira ispit iz druge godine, koji student još ne sluša. Eligibility mora da vrati:

`SUBJECT_NOT_ENROLLED`

### Odjava prijave

Student prijavljuje validan ispit prve godine, zatim ga odjavljuje sa razlogom.

Test proverava:

- status `ODJAVLJEN`;
- sačuvan razlog;
- prisustvo odjavljene prijave u istoriji izlazaka;
- da odjava ne daje ESPB.

### Polaganje svih predmeta

Za svaki predmet prve godine test:

1. kreira predispitnu obavezu;
2. evidentira 30 predispitnih poena;
3. kreira ispit i ispitni rok;
4. prijavljuje studenta;
5. evidentira izlazak i 40 ispitnih poena;
6. zaključava rezultate.

Ukupno je 70 poena po predmetu, što daje ocenu 7.

Posle prve godine test očekuje:

- 60 ESPB;
- prosek `7.0`;
- 4 položena predmeta.

## 7. Upis druge godine

Studentov eligibility mora da predloži:

`ENROLL_NEXT_YEAR`

Student podnosi zahtev. Drugi aktivni zahtev za istu ciljnu godinu mora biti odbijen kodom:

`DUPLICATE_ACTIVE_YEAR_REQUEST`

Admin vidi zahtev i prvo postavlja nepotpunu checklistu. Pokušaj odobravanja mora biti odbijen kodom:

`YEAR_REQUEST_NOT_READY`

Nakon potvrde ugovora, uplate i dokumentacije admin odobrava zahtev.

Test proverava:

- istoriju upisa `2, 1`;
- aktiviranje druge školske godine;
- tačno četiri predmeta druge godine.

## 8. Druga godina: pad i ponovni izlazak

Za jedan predmet druge godine student:

1. dobija predispitne poene;
2. prijavljuje prvi ispit;
3. izlazi na ispit, ali osvaja nedovoljno poena;
4. dobija status `PAO`;
5. prijavljuje isti predmet u sledećem roku;
6. drugi put polaže predmet;
7. dobija status `POLOZIO`.

Test proverava da istorija sadrži oba izlaska i da je broj polaganja jednak 2.

Nakon polaganja pokušaj nove prijave istog predmeta mora biti odbijen kodom:

`SUBJECT_ALREADY_PASSED`

## 9. Poništavanje prijave

Student prijavljuje drugi predmet, a admin poništava prijavu sa razlogom.

Test proverava:

- status `PONISTEN`;
- sačuvan razlog;
- da poništena prijava ne menja broj ESPB;
- da ne utiče pogrešno na prosek i položene predmete.

## 10. Sync predmeta i profesorske dozvole

`syncCurrentSubjects(...)` se poziva dva puta.

Oba poziva moraju ostaviti isti broj predmeta i ne smeju kreirati duplikate.

Test zatim kreira drugog profesora i proverava da on ne može pristupiti ispitu koji mu nije dodeljen. Očekuje se `AccessDeniedException`.

## 11. Završetak druge, treće i četvrte godine

Helper metoda `completeYear(...)` polaže sve preostale nepoložene predmete određene godine.

Nakon završetka druge godine test očekuje 120 ESPB i zatim upisuje treću godinu.

Nakon završetka treće godine test očekuje 180 ESPB i zatim upisuje četvrtu godinu.

Nakon završetka četvrte godine test očekuje:

- 240 ESPB;
- 16 položenih predmeta;
- završni prosek `6.9375`;
- istoriju upisa godina `4, 3, 2, 1`;
- bez dupliranih `SlusaPredmet` zapisa.

Završni prosek nije `7.0` zato što je predmet položen iz drugog pokušaja položen sa ukupno 60 poena, odnosno ocenom 6.

## 12. Uslovni upis

Poseban student dobija tri priznata predmeta prve godine, ukupno 45 ESPB.

Test proverava:

- eligibility predlaže `CONDITIONAL_ENROLLMENT`;
- student bira nepoložen predmet za prenos;
- admin odobrava zahtev;
- student u sledećoj godini vidi četiri nova i jedan preneti predmet;
- nema dupliranih zapisa slušanja.

## 13. Obnova godine

Poseban student nema osvojene ESPB.

Test proverava:

- eligibility predlaže `RENEW_YEAR`;
- student bira nepoložene predmete koje obnavlja;
- admin odobrava zahtev;
- student ostaje na prvoj godini;
- istorija upisa sadrži dva upisa prve godine;
- postoji zapis obnove;
- student vidi izabrane predmete bez duplikata.

## 14. Dodatni negativni scenariji

### Ocena bez izlaska

Studentu se ručno postavlja visoka ocena, ali `daLiJeIzasao` ostaje `false`.

Test zahteva da:

- predmet nije položen;
- student ne dobije ESPB;
- ocena ne ulazi u prosek.

Ovaj scenario je otkrio i pokrio raniju grešku u upitima za položene predmete.

### Indeks bez upisa godine

Kreira se neaktivan indeks bez upisa godine.

Test proverava da takav indeks nema trenutne predmete i da sistem ne vraća tuđe ili pogrešne podatke.

## 15. Šta se stvarno koristi

Test ne koristi hardkodovane postojeće redove iz baze.

Tok koristi stvarne aplikacione servise, između ostalog:

- `StudijskiProgramAdminService`;
- `PredmetAdminService`;
- `SkolskaGodinaService`;
- `RealizacijaPredmetaService`;
- `DrziPredmetAdminService`;
- `IspitAdminService`;
- `IspitCommandService`;
- `PredispitnaObavezaService`;
- `OstvarenaPredObavService`;
- `StudyYearEnrollmentService`;
- `AcademicProgressService`;
- `StudentProfileService`;
- `UpisObnovaService`;
- `UserAccountService`.

Za provere se koriste stvarni JPA repozitorijumi i isti poslovni upiti koje koristi aplikacija.

## 16. Razlika u odnosu na Playwright

`FullStudyLifecycleIT`:

- detaljno simulira sve četiri godine;
- izvršava poslovnu logiku kroz Spring servise;
- proverava bazu, statuse, ESPB, prosek, istoriju i dozvole.

`npm run e2e:live`:

- pokreće stvarni Spring backend i React frontend;
- unosi kredencijale i klikće dugmad u browseru;
- proverava stvarni login za ADMIN, STUDENT i PROFESSOR;
- otvara ključne role rute;
- izvršava reprezentativnu admin mutaciju kroz UI.

Četvorogodišnji scenario nije kompletno ponovljen klik po klik u browseru, zato što bi takav test bio znatno sporiji i osetljiviji. Poslovna ispravnost četvorogodišnjeg toka proverava se determinističkim Spring integration testom.

## 17. Pokretanje samo četvorogodišnjeg testa

Iz direktorijuma `ServerAndDTOs`:

```powershell
$env:JAVA_HOME='C:\Users\gamek\.jdks\ms-11.0.30'
.\mvnw.cmd -pl server -am "-Dtest=FullStudyLifecycleIT" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

## 18. Pokretanje kompletnog pipeline-a

Backend:

```powershell
cd ServerAndDTOs
$env:JAVA_HOME='C:\Users\gamek\.jdks\ms-11.0.30'
.\mvnw.cmd test
```

Frontend:

```powershell
cd web-client
npm run lint
npm run e2e
npm run e2e:live
npm run build
```

Poslednja kompletna verifikacija:

- backend: 55 testova, 0 grešaka;
- brzi Playwright smoke: 3 testa prošla;
- live frontend-backend Playwright: 3 testa prošla;
- frontend lint i build: prošli.
