package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StudentIndeksServiceTest {

    @Test
    void findsFirstGapWhenAllocatingIndexNumber() {
        StudentIndeksService service = new StudentIndeksService(mock(StudentIndeksRepository.class));

        assertThat(service.findNextAvailableNumber(Arrays.asList(1, 2, 4, 5))).isEqualTo(3);
        assertThat(service.findNextAvailableNumber(Arrays.asList(1, 2, 3))).isEqualTo(4);
        assertThat(service.findNextAvailableNumber(Collections.emptyList())).isEqualTo(1);
    }
}
