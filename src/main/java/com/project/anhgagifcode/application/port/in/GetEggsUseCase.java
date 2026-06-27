package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.EggDto;
import java.util.List;

public interface GetEggsUseCase {
    List<EggDto> getEggs();
}
