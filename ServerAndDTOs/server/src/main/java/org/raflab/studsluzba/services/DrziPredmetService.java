package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;

import org.raflab.studsluzba.model.dtos.DrziPredmetNewRequest;
import org.raflab.studsluzba.model.dtos.DrziPredmetRequest;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.NastavnikRepository;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.repositories.SkolskaGodinaRepository;


@Service
@RequiredArgsConstructor
public class DrziPredmetService {

    final DrziPredmetRepository drziPredmetRepository;
    final PredmetRepository predmetRepository;
    final NastavnikRepository nastavnikRepository;
    final SkolskaGodinaRepository skolskaGodinaRepository;


    @Transactional
    public void saveDrziPredmet(DrziPredmetRequest request) {
        throw ApiException.badRequest(
                "Stara putanja za dodelu nastavnika vise nije podrzana. Koristite /api/drzi/create sa realizacijaPredmetaId."
        );
    }
}
