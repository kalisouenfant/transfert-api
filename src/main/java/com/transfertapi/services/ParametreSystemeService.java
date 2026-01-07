package com.transfertapi.services;

import com.transfertapi.entities.ParametreSysteme;
import com.transfertapi.repositories.ParametreSystemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParametreSystemeService {

    @Autowired
    private ParametreSystemeRepository parametreSystemeRepository;

    public List<ParametreSysteme> getAll() {
        return parametreSystemeRepository.findAll();
    }

    public Optional<ParametreSysteme> getByCle(String cle) {
        return parametreSystemeRepository.findByCle(cle);
    }

    public ParametreSysteme save(ParametreSysteme parametre) {
        return parametreSystemeRepository.save(parametre);
    }

    public void delete(Integer id) {
        parametreSystemeRepository.deleteById(id);
    }

    public boolean existsByCle(String cle) {
        return parametreSystemeRepository.existsByCle(cle);
    }
}
