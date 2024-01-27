package dev.siwa.tinnu.tinnu.modele.cadran;

import dev.siwa.tinnu.tinnu.Tinnu;
import dev.siwa.tinnu.tinnu.affichage.Afficheur;
import dev.siwa.tinnu.tinnu.config.TinnuConfig;
import dev.siwa.tinnu.tinnu.modele.Horloge.IHorloge;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;
import static org.bukkit.Bukkit.getServer;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class Cadran implements ICadran {

    private List<World> lesMondes;
    private LocalTime debutJour;
    private LocalTime debutNuit;

    private IHorloge horloge;

    private int positionSoleilDebutJour;
    private int positionLuneDebutNuit;

    private int nbChangementPositionAstreDansCycle;

    public Cadran(IHorloge horloge) {
        this.lesMondes = TinnuConfig.getMondesAffectes();

        this.horloge = horloge;
        this.debutJour = horloge.getHeureLeveSoleil();
        this.debutNuit = horloge.getHeureCoucheSoleil();

        this.positionLuneDebutNuit = TinnuConfig.getPositionDebutNuit();
        this.positionSoleilDebutJour = TinnuConfig.getPositionSoleilDebutJour();
        this.nbChangementPositionAstreDansCycle = TinnuConfig.getNbRepositionnements();
    }

    private int calculDistanceEntreDeuxPositions(int pos1, int pos2) {
        
        int ecart = pos2 - pos1;
        if (ecart < 0) {
            ecart += 24000;
        }

        return ecart;
    }

    private int distanceAParcourirLune() {
        return this.calculDistanceEntreDeuxPositions(this.positionLuneDebutNuit, this.positionSoleilDebutJour);
    }

    private int distanceAParcourirSoleil() {
        return this.calculDistanceEntreDeuxPositions(this.positionSoleilDebutJour, this.positionLuneDebutNuit);
    }

    private int calculDureeEnSecondes(LocalTime debut, LocalTime fin) {
        
        int temps = -2;

        if (debut.isAfter(fin)) {
            if ((debut.isAfter(LocalTime.of(12, 00)) && fin.isAfter(LocalTime.of(12, 00))) ||
                    (debut.isBefore(LocalTime.of(12,00)) && fin.isBefore(LocalTime.of(12, 00)))) {

                LocalTime nouveauDebut = fin;
                LocalTime nouvelleFin = debut;

                temps = LocalTime.of(23, 59, 59).toSecondOfDay() - (int) Duration.between(nouveauDebut, nouvelleFin).toSeconds();
                
            } else {
                
                LocalTime nouveauDebut = debut.plusHours(12);
                LocalTime nouvelleFin = fin.plusHours(12);

                temps = (int) Duration.between(nouveauDebut, nouvelleFin).toSeconds();
            }
        } else {
            temps =(int) Duration.between(debut, fin).toSeconds();
        }

        return temps;
    }

    private int calculDureeNuit() {
        return this.calculDureeEnSecondes(this.debutNuit, this.debutJour);
    }

    private int calculDureeJour() {
        return this.calculDureeEnSecondes(this.debutJour, this.debutNuit);
    }

    private void avancerAstre(int val, World monde) {
        monde.setTime((monde.getTime() + val) % 24000);
    }

    private void positionnerAstreAuBonEndroit(int bonnePosition) {
        for (World w : this.lesMondes) {
            w.setTime(bonnePosition % 24000);
        }
    }

    private int calculDistanceRepositionnementAstre(int distanceTotale, int nbChangementPosition) {

        int resultat = distanceTotale / nbChangementPosition;
        
        if (resultat < 1) {
            resultat = 1;
        }
        
        return resultat;
    }

    private int calculTempsEntreMouvementAstre(int dureeTotale, int nbChangements) {
    
        int resultat = dureeTotale / nbChangements;
        
        if (resultat < 1) {
            resultat = 1;
        }

        return resultat;
    }

    private void lancerCycleNuit() {

        int distanceAParcourir = distanceAParcourirLune();
        int dureeDeLaNuit = calculDureeNuit();
        final int dureeEntreChangements = calculTempsEntreMouvementAstre(dureeDeLaNuit, this.nbChangementPositionAstreDansCycle);
        final int distanceEntreChangements = this.calculDistanceRepositionnementAstre(distanceAParcourir, this.nbChangementPositionAstreDansCycle);

        int tempsARattraper = calculDureeEnSecondes(this.horloge.getHeureCoucheSoleil(), this.horloge.getHeureCourante());
        int distanceARattraper = calculProportion(dureeDeLaNuit, tempsARattraper, distanceAParcourir);

        this.positionnerAstreAuBonEndroit(this.positionLuneDebutNuit + distanceARattraper);

        BukkitScheduler scheduler = getServer().getScheduler();

        scheduler.runTask(Tinnu.getInstance(), () -> {
            this.faireAvancerLune(scheduler, dureeEntreChangements, distanceEntreChangements);
        });
    }

    private boolean isToujoursNuit() {

        LocalTime heureCourante = this.horloge.getHeureCourante();
        if (heureCourante.isAfter(LocalTime.of(12, 00)) &&
                heureCourante.isAfter(this.horloge.getHeureCoucheSoleil())) {

            return true;
        }

        if (heureCourante.isBefore(LocalTime.of(12,00)) &&
                heureCourante.isAfter(this.horloge.getHeureCoucheSoleil())) {

            return true;
        }

        return false;
    }

    private boolean isToujoursJour() {

        if (isToujoursNuit()) {
            return false;
        }

        return true;
    }

    private void faireAvancerLune(BukkitScheduler scheduler, int dureeEntreChangements, int distanceEntreChangements) {

        if (isToujoursNuit()) {
            for (World w : this.lesMondes) {
                this.avancerAstre(distanceEntreChangements, w);
            }

            long tpsAvantProchainChangement = 20 * dureeEntreChangements;

            scheduler.runTaskLater(Tinnu.getInstance(), () -> {
                this.faireAvancerLune(scheduler, dureeEntreChangements, distanceEntreChangements);
            }, tpsAvantProchainChangement);

        } else {
            this.lancerCycleJour();
        }
    }


    private void lancerCycleJour() {

        int distanceAParcourir = distanceAParcourirSoleil();
        int dureeDuJour = calculDureeJour();
        final int dureeEntreChangements = calculTempsEntreMouvementAstre(dureeDuJour, this.nbChangementPositionAstreDansCycle);
        final int distanceEntreChangements = this.calculDistanceRepositionnementAstre(distanceAParcourir, this.nbChangementPositionAstreDansCycle);

        int tempsARattraper = calculDureeEnSecondes(this.horloge.getHeureLeveSoleil(), this.horloge.getHeureCourante());
        int distanceARattraper =  calculProportion(dureeDuJour, tempsARattraper, distanceAParcourir);

        this.positionnerAstreAuBonEndroit(this.positionSoleilDebutJour + distanceARattraper);

        BukkitScheduler scheduler = getServer().getScheduler();

        scheduler.runTask(Tinnu.getInstance(), () -> {
            this.faireAvancerSoleil(scheduler, dureeEntreChangements, distanceEntreChangements);
        });
    }

    private int calculProportion(int dureeTotale, int tempsARattraper, int distanceTotale) {
        
        double proportion = (double) tempsARattraper / dureeTotale;
        int resultat = (int) (distanceTotale * proportion);
        return resultat;
    }

    private void faireAvancerSoleil(BukkitScheduler scheduler, int dureeEntreChangements, int distanceEntreChangements) {
        
        if (isToujoursJour()) {
            for (World w : this.lesMondes) {
                this.avancerAstre(distanceEntreChangements, w);
            }

            long tpsAvantProchainChangement = 20 * dureeEntreChangements;

            scheduler.runTaskLater(Tinnu.getInstance(), () -> {
                this.faireAvancerSoleil(scheduler, dureeEntreChangements, distanceEntreChangements);
            }, tpsAvantProchainChangement);

        } else {
            this.lancerCycleNuit();
        }
    }

    @Override
    public void lancerSynchronisation() {
        
        if (isToujoursNuit()) {
           this.lancerCycleNuit();
        } else {
            this.lancerCycleJour();
        }
    }


    public void setListeMondes(List<World> nouvelleListe) {
        this.lesMondes = nouvelleListe;
    }

    public void ajouterMonde(World nouveauMonde) {
        this.lesMondes.add(nouveauMonde);
    }
}
