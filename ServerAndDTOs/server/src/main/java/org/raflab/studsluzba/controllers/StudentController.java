package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;

import org.raflab.studsluzba.model.dtos.StudentIndeksRequest;
import org.raflab.studsluzba.model.dtos.StudentPodaciRequest;
import org.raflab.studsluzba.model.dtos.StudentIndeksResponse;
import org.raflab.studsluzba.model.dtos.StudentPodaciResponse;
import org.raflab.studsluzba.model.dtos.StudentDTO;
import org.raflab.studsluzba.model.dtos.StudentDashboardDTO;
import org.raflab.studsluzba.model.dtos.StudentProfileDTO;
import org.raflab.studsluzba.model.dtos.StudentWebProfileDTO;
import org.raflab.studsluzba.model.dtos.StudentIndexProvisionDTO;
import org.raflab.studsluzba.services.StudentProfileService;
import org.raflab.studsluzba.services.StudentService;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/*
{
        "id": 10,
        "broj": 5,
        "godina": 2023,
        "studProgramOznaka": "RN",
        "nacinFinansiranja": "BU",
        "aktivan": true,
        "vaziOd": "2023-09-30",
        "student": {
            "id": 16,
            "ime": "Ivan",
            "prezime": "Stankovic",
            "srednjeIme": "M",
            "jmbg": null,
            "datumRodjenja": "2002-05-08",
            "mestoRodjenja": null,
            "mestoPrebivalista": "Beograd",
            "drzavaRodjenja": null,
            "drzavljanstvo": "RS",
            "nacionalnost": null,
            "pol": "M",
            "adresa": "Ulica 1",
            "brojTelefonaMobilni": null,
            "brojTelefonaFiksni": null,
            "emailFakultetski": "istankovic2304rn@raf.rs",
            "emailPrivatni": "ivan.stankovic@gmail.com",
            "brojLicneKarte": null,
            "licnuKartuIzdao": null,
            "mestoStanovanja": null,
            "adresaStanovanja": null
        },
        "studijskiProgram": {
            "id": 2,
            "oznaka": "RN",
            "naziv": "Računarske nauke",
            "godinaAkreditacije": 2023,
            "zvanje": "Dipl. inž.",
            "trajanjeGodina": 4,
            "trajanjeSemestara": 8,
            "vrstaStudija": {
                "id": 2,
                "skracenica": "OAS",
                "puniNaziv": "Osnovne akademske studije"
            },
            "ukupnoEspb": 240
        },
        "ostvarenoEspb": 0
    }

 */


//Zapisao
@RestController
@RequestMapping(path="/api/student")
@RequiredArgsConstructor
@Validated
public class StudentController {

    private final StudentService studentService;
    private final StudentProfileService studentProfileService;
    private final CurrentUser currentUser;

    //Dodavanje novog studenta
    @PostMapping(path="/add")
    public Long addNewStudentPodaci(@RequestBody  @Valid StudentPodaciRequest studentPodaci) {
        return studentService.addNewStudentPodaci(studentPodaci);
    }
    //Vrati sve stuidente
    @GetMapping(path="/all")
    public Iterable<StudentPodaciResponse> getAllStudentPodaci() {
        return studentService.getAllStudentPodaci();
    }
    //vrati sve studnete paginirano
    @GetMapping(path="/svi")
    public Page<StudentPodaciResponse> getAllStudentPodaciPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return studentService.getAllStudentPodaciPaginated(page, size);
    }
    //RADI
    @GetMapping(path="/podaci/{id}")
    public StudentPodaciResponse getStudentPodaci(@PathVariable Long id){
        return studentService.getStudentPodaci(id);
    }


    //RADI
    @PostMapping(path="/saveindeks")
    public Long saveIndeks(@RequestBody @Valid StudentIndeksRequest request) {
        return studentService.saveIndeks(request);
    }

    @PostMapping(path="/saveindeks/provision")
    public StudentIndexProvisionDTO saveIndeksProvision(@RequestBody @Valid StudentIndeksRequest request) {
        return studentService.saveIndeksProvision(request);
    }

    // 5 2023  RN   Ivan stankovic
    //RADI
    @GetMapping(path="/indeks/{id}")
    public StudentIndeksResponse getStudentIndeks(@PathVariable Long id){
        return studentService.getStudentIndeks(id);
    }



    //RADI
    @GetMapping(path="/indeksi/{idStudentPodaci}")
    public java.util.List<StudentIndeksResponse> getIndeksiForStudentPodaciId(@PathVariable Long idStudentPodaci){
        return studentService.getIndeksiForStudentPodaciId(idStudentPodaci);
    }



    //RADI
    @GetMapping(path="/fastsearch")
    public StudentIndeksResponse fastSearch(@RequestParam String indeksShort) {
        return studentService.fastSearch(indeksShort);
    }


    //RADI
    @GetMapping(path="/emailsearch")
    public StudentIndeksResponse emailSearch(@RequestParam String studEmail) {
        return studentService.emailSearch(studEmail);
    }


    //RADI
    @GetMapping(path="/search")
    public Page<StudentDTO> search(@RequestParam (required = false) String ime,
                                   @RequestParam (required = false) String prezime,
                                   @RequestParam (required = false) String studProgram,
                                   @RequestParam (required = false) Integer godina,
                                   @RequestParam (required = false) Integer broj,
                                   @RequestParam(defaultValue = "0") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size) {
        return studentService.search(ime, prezime, studProgram, godina, broj, page, size);
    }

    @GetMapping(path="/global-search")
    public Page<StudentDTO> globalSearch(@RequestParam String q,
                                         @RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "20") Integer size) {
        return studentService.globalSearch(q, page, size);
    }




    //RADI
    @GetMapping(path="/profile/{studentIndeksId}")
    public StudentProfileDTO getStudentProfile(@PathVariable  Long studentIndeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(studentIndeksId);
        return studentService.getStudentProfile(studentIndeksId);
    }

    @GetMapping(path="/dashboard/{studentIndeksId}")
    public StudentDashboardDTO getStudentDashboard(@PathVariable Long studentIndeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(studentIndeksId);
        return studentProfileService.getStudentDashboard(studentIndeksId);
    }


    //RADI
    @GetMapping(path="/webprofile/{studentIndeksId}")
    public StudentWebProfileDTO getStudentWebProfile(@PathVariable  Long studentIndeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(studentIndeksId);
        return studentService.getStudentWebProfile(studentIndeksId);
    }


    //RADI
    @GetMapping(path="/webprofile/email")
    public StudentWebProfileDTO getStudentWebProfileForEmail(@RequestParam String studEmail) {
        currentUser.requireAdmin();
        return studentService.getStudentWebProfileForEmail(studEmail);
    }
}
