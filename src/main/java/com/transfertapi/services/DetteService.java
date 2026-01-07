package com.transfertapi.services;

import com.transfertapi.entities.Dette;
import com.transfertapi.repositories.DetteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetteService {

    @Autowired
    private DetteRepository detteRepository;

    public List<Dette> getByClient(Integer clientId) {
        return detteRepository.findByClientId(clientId);
    }

    public Dette save(Dette dette) {
        return detteRepository.save(dette);
    }

    public void delete(Integer id) {
        detteRepository.deleteById(id);
    }
}
