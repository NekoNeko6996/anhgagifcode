package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.Egg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateEggHatchTimeServiceTest {

    @Mock
    private EggPersistencePort eggPersistencePort;

    @InjectMocks
    private UpdateEggHatchTimeService service;

    @Test
    void testUpdateHatchTime_Success() {
        Egg egg = Egg.builder()
                .id("egg-1")
                .status("HATCHING")
                .build();

        when(eggPersistencePort.findById("egg-1")).thenReturn(Optional.of(egg));
        when(eggPersistencePort.saveEgg(any(Egg.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime newHatchTime = LocalDateTime.now().plusDays(1);
        service.updateHatchTime("egg-1", newHatchTime);

        assertEquals(newHatchTime, egg.getHatchAt());
        assertEquals("HATCHING", egg.getStatus());
        verify(eggPersistencePort, times(1)).saveEgg(egg);
    }

    @Test
    void testUpdateHatchTime_PastTime_SetsReadyToClaim() {
        Egg egg = Egg.builder()
                .id("egg-1")
                .status("HATCHING")
                .build();

        when(eggPersistencePort.findById("egg-1")).thenReturn(Optional.of(egg));
        when(eggPersistencePort.saveEgg(any(Egg.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
        service.updateHatchTime("egg-1", pastTime);

        assertEquals(pastTime, egg.getHatchAt());
        assertEquals("READY_TO_CLAIM", egg.getStatus());
        verify(eggPersistencePort, times(1)).saveEgg(egg);
    }

    @Test
    void testUpdateHatchTime_NotFound_ThrowsException() {
        when(eggPersistencePort.findById("invalid-egg")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.updateHatchTime("invalid-egg", LocalDateTime.now());
        });

        verify(eggPersistencePort, never()).saveEgg(any(Egg.class));
    }
}
