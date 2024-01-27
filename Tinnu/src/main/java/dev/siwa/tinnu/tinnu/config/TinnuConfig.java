package dev.siwa.tinnu.tinnu.config;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

import static org.bukkit.Bukkit.getServer;

public class TinnuConfig {

    private static List<World> mondesAffectes;
    private static boolean tinnuActif;

    private static boolean configChargee = false;
    private static LocalTime heureLeveSoleil;
    private static LocalTime heureCoucheSoleil;

    private static int positionSoleilDebutJour;
    private static int positionSoleilDebutNuit;

    private static int nbRepositionnements;

    public static void chargerConfig(FileConfiguration fichierConfig) {

        configChargee = true;
        tinnuActif = fichierConfig.getBoolean("plugin-actif", true);

        heureLeveSoleil = LocalTime.parse(fichierConfig.getString("heure-lever-soleil", "07:00"));
        heureCoucheSoleil = LocalTime.parse(fichierConfig.getString("heure-coucher-soleil", "21:00"));


        positionSoleilDebutNuit = fichierConfig.getInt("position-soleil-nuit", 13201);
        positionSoleilDebutJour = fichierConfig.getInt("position-soleil-jour", 22571);

        nbRepositionnements = fichierConfig.getInt("nb-repostionnements", 20);

        List<String> nomDesMondes = fichierConfig.getStringList("mondes-supportes");

        mondesAffectes = new ArrayList<>();

        for (String nomMonde: nomDesMondes) {

            mondesAffectes.add(getServer().getWorld(nomMonde));

        }
    }

//    public static String afficherConfig() {
//
//        StringBuilder infos = new StringBuilder();
//        infos.append("----- Configuration Tinnu -----\n");
//        infos.append("Tinnu Actif : ").append(tinnuActif).append("\n");
//        infos.append("Heure de lever du soleil : ").append(heureLeveSoleil).append("\n");
//        infos.append("Heure de coucher du soleil : ").append(heureCoucheSoleil).append("\n");
//        infos.append("Position du soleil au début du jour : ").append(positionSoleilDebutJour).append("\n");
//        infos.append("Position du soleil au début de la nuit : ").append(positionSoleilDebutNuit).append("\n");
//
//        if (isConfigChargee()) {
//            return infos.toString();
//        } else {
//            return ("Config de Tinnu non chargée ! ");
//        }
//    }

    public static String afficherConfig() {
        StringBuilder infos = new StringBuilder();
        infos.append("----- Configuration Tinnu -----\n");
        infos.append("Tinnu Actif : ").append(tinnuActif).append("\n");
        infos.append("Heure de lever du soleil : ").append(heureLeveSoleil).append("\n");
        infos.append("Heure de coucher du soleil : ").append(heureCoucheSoleil).append("\n");
        infos.append("Position du soleil au début du jour : ").append(positionSoleilDebutJour).append("\n");
        infos.append("Position du soleil au début de la nuit : ").append(positionSoleilDebutNuit).append("\n");

        // Ajout de la liste des mondes affectés
        infos.append("Mondes affectes : ");
        if (mondesAffectes != null && !mondesAffectes.isEmpty()) {
            for (World monde : mondesAffectes) {
                infos.append(monde.getName()).append(", ");
            }
            infos.delete(infos.length() - 2, infos.length()); // Supprime la virgule et l'espace après le dernier monde
        } else {
            infos.append("Aucun monde spécifié");
        }
        infos.append("\n");

        if (isConfigChargee()) {
            return infos.toString();
        } else {
            return ("Config de Tinnu non chargée ! ");
        }
    }


    public static  boolean isConfigChargee() {
        return configChargee;
    }

    public static List<World> getMondesAffectes() {
        return mondesAffectes;
    }

    public static boolean isTinnuActif() {
        return tinnuActif;
    }

    public static int getNbRepositionnements() {
        return nbRepositionnements;
    }
    public static LocalTime getHeureLeveSoleil() {
        return heureLeveSoleil;
    }

    public static  LocalTime getHeureCoucheSoleil() {
        return  heureCoucheSoleil;
    }

    public static int getPositionSoleilDebutJour() {
        return positionSoleilDebutJour;
    }

    public static int getPositionDebutNuit() {
        return positionSoleilDebutNuit;    }

}
