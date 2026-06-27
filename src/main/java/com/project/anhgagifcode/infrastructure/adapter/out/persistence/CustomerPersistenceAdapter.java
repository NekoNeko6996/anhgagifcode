package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.CustomerPersistencePort;
import com.project.anhgagifcode.domain.model.Customer;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Customers;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.CustomerMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerPersistencePort {

    private final CustomerJpaRepository repository;
    private final CustomerMapper mapper;

    @Override
    public Optional<Customer> loadByCustomerCode(String customerCode) {
        return repository.findByCustomerCode(customerCode)
                .map(mapper::toDomain);
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        Customers entity = mapper.toEntity(customer);
        Customers savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}