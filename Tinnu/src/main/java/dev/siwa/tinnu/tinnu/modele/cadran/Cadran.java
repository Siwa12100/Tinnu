package dev.siwa.tinnu.tinnu.modele.cadran;

import dev.siwa.tinnu.tinnu.Tinnu;
import dev.siwa.tinnu.tinnu.affichage.Afficheur;
import dev.siwa.tinnu.tinnu.modele.Horloge.IHorloge;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class Cadran implements ICadran {

    private List<World> lesMondes;
    private LocalTime debutJour;
    private LocalTime debutNuit;

    private IHorloge horloge;

    private int positionSoleilDebutJour;
    private int positionLuneDebutNuit;

    private int nbChangementPositionAstreDansCycle;

    public Cadran(IHorloge horloge) {
        this.lesMondes = new ArrayList<>();

        this.horloge = horloge;
        this.debutJour = horloge.getHeureLeveSoleil();
        this.debutNuit = horloge.getHeureCoucheSoleil();

        this.positionLuneDebutNuit = 13200;
        this.positionSoleilDebutJour = 22570;
        this.nbChangementPositionAstreDansCycle = 300;
    }

    private int calculDistanceEntreDeuxPositions(int pos1, int pos2) {
        int ecart = pos2 - pos1;
        Afficheur.afficherDebug("Debut de calcul de l'ecart entre distances ( " + pos1 + "," + pos2 + "). L'ecart est de " + ecart + " !");

        if (ecart < 0) {
            ecart += 24000;
        }

        Afficheur.afficherDebug("L'Ecart etait negatif, il est mtn a " + ecart + " !");
        return ecart;
    }

    private int distanceAParcourirLune() {
        return this.calculDistanceEntreDeuxPositions(this.positionLuneDebutNuit, this.positionSoleilDebutJour);
    }

    private int distanceAParcourirSoleil() {
        return this.calculDistanceEntreDeuxPositions(this.positionSoleilDebutJour, this.positionLuneDebutNuit);
    }

    private int calculDureeEnSecondes(LocalTime debut, LocalTime fin) {
        Afficheur.afficherDebug("Debut du calcul du temps entre " + debut + " et " + fin + ".");

        int temps = -2;

        if (debut.isAfter(fin)) {
            Afficheur.afficherDebug("debut apres la fin...");
            if ((debut.isAfter(LocalTime.of(12, 00)) && fin.isAfter(LocalTime.of(12, 00))) ||
                    (debut.isBefore(LocalTime.of(12,00)) && fin.isBefore(LocalTime.of(12, 00)))) {

                Afficheur.afficherDebug("passage dans before before after after");

                LocalTime nouveauDebut = fin;
                LocalTime nouvelleFin = debut;

                temps = LocalTime.of(23, 59, 59).toSecondOfDay() - (int) Duration.between(nouveauDebut, nouvelleFin).toSeconds();
                Afficheur.afficherDebug("avec nouveau sec : " + temps);

            } else {

                Afficheur.afficherDebug("sinon passage dans le ici classique.....");
                LocalTime nouveauDebut = debut.plusHours(12);
                LocalTime nouvelleFin = fin.plusHours(12);

                temps = (int) Duration.between(nouveauDebut, nouvelleFin).toSeconds();

                Afficheur.afficherDebug("avec nouveau sec : " + temps);
            }

        } else {

            Afficheur.afficherDebug("Fin apres le debut...");
            temps =(int) Duration.between(debut, fin).toSeconds();

        }

        Afficheur.afficherDebug("La duree est donc de " + temps + ".");
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

//        if (nbChangementPosition == 0) {
//            nbChangementPosition = 1;
//        }

        Afficheur.afficherDebug("Calcul de la distance entre repositionnements de l'astre (distance totale = " +
                distanceTotale + ", nbRepositionnements = " + nbChangementPosition + ").");

        int resultat = distanceTotale / nbChangementPosition;
        Afficheur.afficherDebug("resultat brut : " + resultat);

        if (resultat < 1) {
            resultat = 1;
        }
        Afficheur.afficherDebug("Au final la distance entre repositionnements est de : " + resultat + ".");

        return resultat;
    }

    private int calculTempsEntreMouvementAstre(int dureeTotale, int nbChangements) {
//        if (nbChangements == 0) {
//            nbChangements = 1;
//        }

        Afficheur.afficherDebug("Calcul du temps entre mouvements de l'astre (duree totale = " + dureeTotale +
                ", nbRepositionnements = " + nbChangements + "). ");

        int resultat = dureeTotale / nbChangements;

        Afficheur.afficherDebug("resultat brut : " + resultat);

        if (resultat < 1) {
            resultat = 1;
        }

        Afficheur.afficherDebug("le temps sera donc de " + resultat + " sec entre repositionnements");
        return resultat;
    }

    private void lancerCycleNuit() {

        Afficheur.afficherDebug("Debut d'un cyle de nuit");



        int distanceAParcourir = distanceAParcourirLune();
        int dureeDeLaNuit = calculDureeNuit();
        final int dureeEntreChangements = calculTempsEntreMouvementAstre(dureeDeLaNuit, this.nbChangementPositionAstreDansCycle);
        final int distanceEntreChangements = this.calculDistanceRepositionnementAstre(distanceAParcourir, this.nbChangementPositionAstreDansCycle);

        int tempsARattraper = calculDureeEnSecondes(this.horloge.getHeureCoucheSoleil(), this.horloge.getHeureCourante());
        int distanceARattraper = 0;

        if (tempsARattraper > 0) {

            Afficheur.afficherDebug("Il va falloir commencer à rattraper " + tempsARattraper + " secondes !");

            int nbFoisARepositionner = tempsARattraper / dureeEntreChangements;
            distanceARattraper = nbFoisARepositionner * distanceEntreChangements;
        }

        this.positionnerAstreAuBonEndroit(this.positionLuneDebutNuit + distanceARattraper);

        Afficheur.afficherDebug("pos world sol : " + getServer().getWorld("world").getTime());

        Afficheur.afficherDebug("Distance a parcourir = " + distanceAParcourir +
                ", duree de la nuit = " + dureeDeLaNuit + "sec, duree entre changements = " + dureeEntreChangements +
                " sec, distance entre changements = " + distanceEntreChangements + ".");

        BukkitScheduler scheduler = getServer().getScheduler();

//        scheduler.runTaskAsynchronously(Tinnu.getInstance(), () -> {
//            this.faireAvancerLune(scheduler, dureeEntreChangements, distanceEntreChangements);
//        });

        scheduler.runTask(Tinnu.getInstance(), () -> {
            this.faireAvancerLune(scheduler, dureeEntreChangements, distanceEntreChangements);
        });


    }

    private boolean isToujoursNuit() {

        LocalTime heureCourante = this.horloge.getHeureCourante();
        Afficheur.afficherDebug("Heure courante : " + heureCourante);

        if (heureCourante.isAfter(LocalTime.of(12, 00)) &&
                heureCourante.isAfter(this.horloge.getHeureCoucheSoleil())) {

            Afficheur.afficherDebug("L'heure courante est bien après 12h et après " + this.horloge.getHeureCoucheSoleil());
            Afficheur.afficherDebug("c'est la nuit ! ");
            return true;
        }

        if (heureCourante.isBefore(LocalTime.of(12,00)) &&
                heureCourante.isAfter(this.horloge.getHeureCoucheSoleil())) {

            Afficheur.afficherDebug("L'heure courante est bien avant 12h et avant " + this.horloge.getHeureLeveSoleil());
            Afficheur.afficherDebug("c'est la nuit ! ");
            return true;
        }

        return false;
    }

    private boolean isToujoursJour() {

        if (isToujoursNuit()) {
            return false;
        }

        Afficheur.afficherDebug("c'est le jour !  ");
        return true;
    }

    private void faireAvancerLune(BukkitScheduler scheduler, int dureeEntreChangements, int distanceEntreChangements) {

        if (isToujoursNuit()) {

            for (World w : this.lesMondes) {
                this.avancerAstre(distanceEntreChangements, w);
            }

            long tpsAvantProchainChangement = 20 * dureeEntreChangements;

//            scheduler.runTaskLaterAsynchronously(Tinnu.getInstance(), () -> {
//                this.faireAvancerLune(scheduler, dureeEntreChangements, distanceEntreChangements);
//            }, tpsAvantProchainChangement);

            scheduler.runTaskLater(Tinnu.getInstance(), () -> {
                this.faireAvancerLune(scheduler, dureeEntreChangements, distanceEntreChangements);
            }, tpsAvantProchainChangement);

        } else {
            this.lancerCycleJour();
        }
    }


    private void lancerCycleJour() {

        Afficheur.afficherDebug("Debut d'un cyle de jour");

        int distanceAParcourir = distanceAParcourirSoleil();
        int dureeDuJour = calculDureeJour();
        final int dureeEntreChangements = calculTempsEntreMouvementAstre(dureeDuJour, this.nbChangementPositionAstreDansCycle);
        final int distanceEntreChangements = this.calculDistanceRepositionnementAstre(distanceAParcourir, this.nbChangementPositionAstreDansCycle);

        int tempsARattraper = calculDureeEnSecondes(this.horloge.getHeureLeveSoleil(), this.horloge.getHeureCourante());
        int distanceARattraper = 0;

        if (tempsARattraper > 0) {

            Afficheur.afficherDebug("Il va falloir commencer à rattraper " + tempsARattraper + " secondes !");

            int nbFoisARepositionner = tempsARattraper / dureeEntreChangements;
            distanceARattraper = nbFoisARepositionner * distanceEntreChangements;

            Afficheur.afficherDebug("La distance a rattraper est de " + distanceARattraper + "(tot : "+ distanceAParcourir + ").");
        }

        this.positionnerAstreAuBonEndroit(this.positionSoleilDebutJour + distanceARattraper);

        Afficheur.afficherDebug("pos world sol : " + getServer().getWorld("world").getTime());

        Afficheur.afficherDebug("Distance a parcourir = " + distanceAParcourir +
                ", duree du jour = " + dureeDuJour + "sec, duree entre changements = " + dureeEntreChangements +
                " sec, distance entre changements = " + distanceEntreChangements + ".");

        BukkitScheduler scheduler = getServer().getScheduler();

//        scheduler.runTaskAsynchronously(Tinnu.getInstance(), () -> {
//            this.faireAvancerLune(scheduler, dureeEntreChangements, distanceEntreChangements);
//        });

        scheduler.runTask(Tinnu.getInstance(), () -> {
            this.faireAvancerSoleil(scheduler, dureeEntreChangements, distanceEntreChangements);
        });
    }

    private void faireAvancerSoleil(BukkitScheduler scheduler, int dureeEntreChangements, int distanceEntreChangements) {
        //Afficheur.afficherDebug("Passage dans le faireAvancerSoleil");
        if (isToujoursJour()) {

            for (World w : this.lesMondes) {
                this.avancerAstre(distanceEntreChangements, w);
                //Afficheur.afficherDebug("On bouge le soleil de " + distanceEntreChangements);
            }

            long tpsAvantProchainChangement = 20 * dureeEntreChangements;

//            scheduler.runTaskLaterAsynchronously(Tinnu.getInstance(), () -> {
//                this.faireAvancerLune(scheduler, dureeEntreChangements, distanceEntreChangements);
//            }, tpsAvantProchainChangement);

            scheduler.runTaskLater(Tinnu.getInstance(), () -> {
                this.faireAvancerSoleil(scheduler, dureeEntreChangements, distanceEntreChangements);
            }, tpsAvantProchainChangement);

        } else {
            this.lancerCycleNuit();
        }
    }

    @Override
    public void lancerSynchronisation() {
        Afficheur.afficherDebug("Lancement de le synchronisation !");

        if (isToujoursNuit()) {

            Afficheur.afficherDebug("C'est la nuit, on lance un cycle de nuit");
            this.lancerCycleNuit();

        } else {
            Afficheur.afficherDebug("C'est le jour, on lance un cycle de jour");
            this.lancerCycleJour();
        }
    }


    public void setListeMondes(List<World> nouvelleListe) {
        this.lesMondes = nouvelleListe;
    }

    public void ajouterMonde(World nouveauMonde) {
        this.lesMondes.add(nouveauMonde);
        Afficheur.afficherDebug("Ajout du monde " + nouveauMonde.getName() + " au cadran.");
    }
}
