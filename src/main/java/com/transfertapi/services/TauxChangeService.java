package com.transfertapi.services;

import com.transfertapi.entities.Devise;
import com.transfertapi.entities.TauxChange;
import com.transfertapi.repositories.TauxChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class TauxChangeService {

    private static final int SCALE = 2; // Précision de 2 chiffres après la virgule pour les montants finaux
    private static final int PRECISION_SCALE = 10; // Précision pour les calculs de taux intermédiaires

    @Autowired
    private TauxChangeRepository tauxChangeRepository;
    
    // Assurez-vous d'avoir un DeviseService pour valider les codes de devise avant de sauvegarder
    @Autowired
    private DeviseService deviseService; 

    public List<TauxChange> getAll() {
        return tauxChangeRepository.findAll();
    }

    public Optional<TauxChange> getById(Integer id) {
        return tauxChangeRepository.findById(id);
    }

    // Gardons cette méthode, mais la suivante est meilleure pour la conversion
    public Optional<TauxChange> getByCodes(String source, String cible) {
        return tauxChangeRepository.findByDeviseSourceCodeAndDeviseCibleCode(source, cible);
    }
    
    /**
     * Récupère l'entité TauxChange la plus récente pour une paire.
     * Cette méthode dépend de la mise à jour dans le TauxChangeRepository.
     */
    public Optional<TauxChange> getLatestTauxChange(String sourceCode, String cibleCode) {
        return tauxChangeRepository.findLatestRate(sourceCode, cibleCode);
    }

    @Transactional
    public TauxChange save(TauxChange taux) {
        // --- Validation et préparation ---
        if (taux.getDeviseSource() == null || taux.getDeviseCible() == null || taux.getTaux() == null) {
             throw new IllegalArgumentException("La devise source, la devise cible et le taux sont requis.");
        }
        
        if (taux.getTaux().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le taux doit être strictement positif.");
        }

        // Récupérer les entités Devise complètes (pour assurer la cohérence JPA)
        Devise source = deviseService.getByCode(taux.getDeviseSource().getCode())
                .orElseThrow(() -> new IllegalArgumentException("Devise source introuvable avec le code: " + taux.getDeviseSource().getCode()));
        
        Devise cible = deviseService.getByCode(taux.getDeviseCible().getCode())
                .orElseThrow(() -> new IllegalArgumentException("Devise cible introuvable avec le code: " + taux.getDeviseCible().getCode()));

        taux.setDeviseSource(source);
        taux.setDeviseCible(cible);
        // ---------------------------------
        
        return tauxChangeRepository.save(taux);
    }

    public void delete(Integer id) {
        tauxChangeRepository.deleteById(id);
    }

    /**
     * Calcule le montant converti, gérant à la fois les taux directs et les taux inverses.
     * @param montant Montant initial à convertir.
     * @param sourceCode Code de la devise de départ (ex: "XOF").
     * @param cibleCode Code de la devise d'arrivée (ex: "LRD").
     * @return Le montant converti.
     * @throws IllegalStateException Si le taux n'est pas trouvé.
     */
    public BigDecimal convertir(BigDecimal montant, String sourceCode, String cibleCode) {
        // Cas 1: Même devise, on retourne le montant arrondi
        if (sourceCode.equalsIgnoreCase(cibleCode)) {
            return montant.setScale(SCALE, RoundingMode.HALF_UP); 
        }

        // 2. Recherche du taux direct (Source -> Cible)
        Optional<TauxChange> directRate = getLatestTauxChange(sourceCode, cibleCode);
        
        // 3. Si le taux direct n'existe pas, on cherche le taux inverse (Cible -> Source)
        if (directRate.isEmpty()) {
            Optional<TauxChange> inverseRate = getLatestTauxChange(cibleCode, sourceCode);
            
            if (inverseRate.isPresent()) {
                // Taux inverse trouvé : Taux direct = 1 / Taux inverse
                BigDecimal inverseTaux = inverseRate.get().getTaux();
                // On divise pour obtenir le taux direct avec une haute précision (10 chiffres)
                BigDecimal taux = BigDecimal.ONE.divide(inverseTaux, PRECISION_SCALE, RoundingMode.HALF_UP); 
                
                // Conversion et arrondi final
                return montant.multiply(taux).setScale(SCALE, RoundingMode.HALF_UP);
            }
        }
        
        // 4. Taux direct trouvé
        if (directRate.isPresent()) {
            BigDecimal taux = directRate.get().getTaux();
            // Conversion et arrondi final
            return montant.multiply(taux).setScale(SCALE, RoundingMode.HALF_UP);
        }

        // 5. Aucun taux trouvé
        throw new IllegalStateException("Taux de change non configuré pour la paire " + sourceCode + "/" + cibleCode + " (ni direct, ni inverse).");
    }
}