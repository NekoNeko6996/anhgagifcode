package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.KiotvietProductDto;
import java.util.List;

public interface GetKiotvietProductsUseCase {
    List<KiotvietProductDto> getProducts();
}
