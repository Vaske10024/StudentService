package org.raflab.studsluzba.controllers;import lombok.RequiredArgsConstructor;import org.raflab.studsluzba.services.ReportingService;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ReportingController{private final ReportingService service;
 @GetMapping(value="/active-students.csv",produces="text/csv")public byte[]active(){return service.activeStudents();}
 @GetMapping(value="/debts.csv",produces="text/csv")public byte[]debts(){return service.debts();}
 @GetMapping(value="/pass-rates.csv",produces="text/csv")public byte[]rates(){return service.passRates();}
}
