package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.util.List;

@Data
public class DrziPredmetRequest {

    List<DrziPredmetNewRequest> drziPredmet;
    List<DrziPredmetNewRequest> newDrziPredmet;

}
