package dev.siwa.tinnu.horloge;
import dev.siwa.tinnu.Tinnu;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.help.GenericCommandHelpTopic;

import java.time.LocalTime;

public class Horloge implements CommandExecutor {

    private boolean enMarche;
    private LocalTime heureActuelleFactice;
    private final int debutJour;
    private final int debutNuit;
    private final int tempsNuit;
    private final int tempsJour;
    public Tinnu tinnu ;


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        // init. des commandes jour, nuit, soir et aube.
        if (initialisationCommandesBasiques(sender, cmd, msg, args)){
            return true;
        }
        System.out.println("\n\n[temp] : Passage après init. Commandes basiques...\n\n");

        if (cmd.getName().equalsIgnoreCase("tinnu")) {
            if (args.length == 6 && args[0].equalsIgnoreCase("synchronisationJourMannuelle")) {
                LocalTime debut = LocalTime.of(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
                LocalTime fin = LocalTime.of(Integer.valueOf(args[3]), Integer.valueOf(args[4]));
                int nbPassages = Integer.valueOf(args[5]);
                System.out.println("[Infos] : Commande synchroJourMannuelle bien trouvée + bons arguments.");

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.sendMessage("[tinnu] : Vous lancez manuellement la synchronisation du jour avec " + args[1] + "h"+ args[2] + " en heure de debut du matin, " + args[3] + "h" + args[4] + " en heure de fin de la soiree et " + nbPassages + " changement de position du soleil dans la journee.");
                    gestionJour(player, debut, fin, nbPassages);
                    return true;
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("tinnu")) {
            System.out.println("Args: " + Arrays.toString(args));
            if (args.length == 6 && args[0].equalsIgnoreCase("synchronisationNuitMannuelle")) {
                LocalTime debut = LocalTime.of(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
                LocalTime fin = LocalTime.of(Integer.valueOf(args[3]), Integer.valueOf(args[4]));
                int nbPassages = Integer.valueOf(args[5]);
                System.out.println("[Infos] : Commande synchroJourMannuelle bien trouvée + bons arguments.");

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.sendMessage("[tinnu] : Vous lancez manuellement la synchronisation du jour avec " + args[1] + "h"+ args[2] + " en heure de debut du matin, " + args[3] + "h" + args[4] + " en heure de fin de la soiree et " + nbPassages + " changement de position du soleil dans la journee.");
                    gestionNuit(player, debut, fin, nbPassages);
                    return true;
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("tinnu")) {
            System.out.println("Args: " + Arrays.toString(args));
            if (args.length == 6 && args[0].equalsIgnoreCase("synchroJourNuit")) {
                LocalTime debut = LocalTime.of(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
                LocalTime fin = LocalTime.of(Integer.valueOf(args[3]), Integer.valueOf(args[4]));
                int nbPassages = Integer.valueOf(args[5]);
                System.out.println("[Infos] : Commande synchroJourNuit bien trouvée + bons arguments.");

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.sendMessage("[tinnu] : Vous lancez manuellement la synchronisation du jour nuit " + args[1] + "h"+ args[2] + " en heure de debut du matin, " + args[3] + "h" + args[4] + " en heure de fin de la soiree et " + nbPassages + " changement de position du soleil dans la journee.");
                    if (debut.isBefore(getHeureActuelle()) && fin.isAfter(getHeureActuelle())){
                        gestionJour(player, debut, fin, nbPassages);
                    }
                    else
                    {
                        gestionNuit(player, fin, debut,  nbPassages);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private int setMinutesTotales(LocalTime debut, LocalTime fin) {

        int val = -1;

        // cas d'une nuit classique avec debut après 12h et fin avant 12h
        if (debut.isAfter(fin) && debut.isAfter(LocalTime.of(12,00)) && fin.isBefore(LocalTime.of(12, 00))){
            LocalTime newFin = fin; // .plusHours(12);
            LocalTime minuit = LocalTime.of(23, 59);
            val = ((minuit.getHour() * 60) + minuit.getMinute() - ((debut.getHour() *  60 ) + debut.getMinute()));
            val = val + ((fin.getHour() * 60 ) + fin.getMinute());
            System.out.println("[infos] : Le nombre de minutes totales est de : " + val + "min.");
            return val;
        }

        // Cas d'un jour classique, debut avant 12h et fin après 12h
        if (debut.isBefore(fin) && debut.isBefore(LocalTime.of(12,00)) && fin.isAfter(LocalTime.of(12,00))){
            val = ((fin.getHour() * 60) + fin.getMinute() - ((debut.getHour() * 60) + debut.getMinute()));
            System.out.println("[infos] : Le nombre de minutes totales est de : " + val + "min.");
            return val;
        }

        // Les autres cas ne sont pas pris en compte pour l'instant
        System.out.println("[Erreurs] : Les horaires renseignés sont imcompatibles...");
        return val;
    }

    private int setTempsARattraper(LocalTime debut, LocalTime fin, int mode) {

        int val = -1;
        // Mode pour gestion jour
        if(mode == 1){
            val = (((fin.getHour() * 60) + fin.getMinute()) - ((debut.getHour() * 60) + debut.getMinute()));
            System.out.println("[infos] le temps à rattraper est de : " + val);
            return val;
        }

        // Mode pour gestion nuit
        if (mode == 2){
            // Si le début initial est après 12 et temps actuel  aussi et debut initial avant temps actuel
            if (debut.isAfter(LocalTime.of(12,00)) && fin.isAfter(LocalTime.of(12,00)) && fin.isAfter(debut)){
                val = (((fin.getHour() * 60) + fin.getMinute()) - ((debut.getHour() * 60) + debut.getMinute()));
                System.out.println("[infos] le temps à rattraper est de : " + val);
                return val;
            }

            if (debut.isAfter(LocalTime.of(12,00)) && fin.isBefore(LocalTime.of(12,00)) && debut.isAfter(fin)){
                LocalTime minuit = LocalTime.of(23, 59);
                val = (((minuit.getHour() * 60) + minuit.getMinute()) - ((debut.getHour() * 60) + debut.getMinute()));
                val = val + ((fin.getHour() * 60) + fin.getMinute());
                System.out.println("[infos] le temps à rattraper est de : " + val);
                return val;
            }
        }

        System.out.println("[infos] Soucis dans le calcul du temps a rattraper");
        return val;
    }

    private void gestionJour(Player p, LocalTime debut, LocalTime fin, int nbPassages) {

        System.out.println("\n\n[Temp] : Lancement de la fonction gestionJour\n\n");

        // Verification des heures données
        if (!debut.isBefore(LocalTime.of(12,00)) || !fin.isAfter(LocalTime.of(12,00))){
            System.out.println("[erreur] : erreur dans les valeurs données à la fonction gestionJour, arret de la fonction.");
            return;
        }

        // On initialise la position actuelle du soleil au début de la journée
        int posSoleilActuelle = debutJour;

        // On initialise le soleil au début de la journée
        p.getWorld().setTime(posSoleilActuelle);
        System.out.println("[infos] : soleil a ete place à l'aube, pos actuelle : " + posSoleilActuelle + "...");

        // On vérifie qu'on récupère bien l'heure actuelle 
        if (getHeureActuelle() == null) {
            System.out.println("[Erreur] : Heure factice est nulle ");
            return;
        }

        // On vérifie que la valeur de début n'est pas nulle 
        if (debut == null) {
            System.out.println("[Erreur] : debut est null ");
            return;
        }

        // On vérifie que la valeur de fin n'est ps nulle 
        if (fin == null) {
            System.out.println("[Erreur] : fin est null ");
            return;
        }

        // On s'assure que le cycle Jour / nuit est désactivé
        p.getWorld().setGameRuleValue("doDaylightCycle", "false");

        // Nombre total de minutes constituant la journée à gérer
        int minutesTotales = setMinutesTotales(debut, fin);
        if (minutesTotales == -1){
            System.out.println("[erreur] : Soucis dans le calcul des minutes totales, fin de la fonction.");
            return;
        }

        // On définit chaque combien de SECONDES on fait bouger le soleil
        double valtemp = (double) minutesTotales / nbPassages * 60;
        int incrementationVraiHeure = (int)valtemp;
        System.out.println("[infos] : Incrementation vrai heure : "+ incrementationVraiHeure + " secondes et valtemp : " + valtemp + "secondes");

        // On définit de combien on fait bouger le soleil à chaque fois 
        int incrementationMinecraft = this.tempsJour / nbPassages;

        // On récupère le temps à rattraper depuis le début du jour (en minutes)
        double tempsARattraper = setTempsARattraper(debut, getHeureActuelle(),1);

        double cpt = 0;
        if (tempsARattraper > 0 ) {
            System.out.println("[infos] :heure théorique de début de la fonction : " + debut + "h");
            System.out.println("[infos] : heure réeelle de début de la fonction : " + getHeureActuelle() +"h");
            System.out.println("[infos] : Le temps à rattraper est donc de : " + tempsARattraper + "minutes.");

            cpt = incrementationMinecraft * ((tempsARattraper / minutesTotales) * nbPassages);
            System.out.println("[infos] :((tempsARattraper / minutesTotales) * nbPassages) -->  " + ((tempsARattraper / minutesTotales) * nbPassages));
            System.out.println("[infos] : incrementation mc : " + incrementationMinecraft);
            System.out.println("[infos] : (int)cpt (cpt : " + cpt + " ) : " + (int)cpt);
        }

        posSoleilActuelle = posSoleilActuelle + (int)cpt;
        System.out.println("[infos] : pos actuelle : " + posSoleilActuelle);

        if (posSoleilActuelle > 23999){
            posSoleilActuelle = posSoleilActuelle % 24000;
            System.out.println("[infos] : pos actuelle < 23999 donc remise a niveau % 24000 ; pos actuelle mtn : " + posSoleilActuelle);
        }

        p.getWorld().setTime(posSoleilActuelle);
        System.out.println("[infos] : Le soleil rattrape son retard de " + cpt + " et est mtn a " + posSoleilActuelle + ".");

        System.out.print("\n\n\n\n[Temp] : Bilan des variables : ");
        System.out.println("\t - debut : " + debut);
        System.out.println("\t - fin : " + fin);
        System.out.println("\t - nbRepetitions : " + nbPassages);
        System.out.println("\t - minutes totales : " + minutesTotales);
        System.out.println("\t - Heure actuelle : " + getHeureActuelle());
        System.out.println("\t - Temps à rattraper : " + tempsARattraper);
        System.out.println("\t - incrementation vrai heure : " + incrementationVraiHeure + " secondes");
        System.out.println("\t - incrementationMinecraft : " + incrementationMinecraft);
        System.out.println("\t - att. debut jour : " + debutJour);
        System.out.println("\t - att. debut nuit : " + debutNuit);
        System.out.println("\t - temps jour  : " + tempsJour);
        System.out.println("\t - temps nuit  : " + tempsNuit);
        System.out.println("\n\n\n\n");


        System.out.println("[Infos: passsage juste avant la decla. du runnable dans gestion jour ");
        BukkitRunnable tache = new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("[infos] : Passage dans un run...");
                System.out.println("[infos] : act  : " + p.getWorld().getFullTime() % 24000 + " debut nuit : " + debutNuit);

                if (!enMarche) {
                    p.getWorld().setTime(debutNuit);
                    System.out.println("[infos] : arret du run en cours car !enMarche");
                    cancel();
                    allumerHorloge();
                    return;
                }

                if (fin.isBefore(getHeureActuelle())){
                    p.getWorld().setTime(debutNuit);
                    System.out.println("La fin de journée (" + fin + ") est avant l'heure actuelle("+ getHeureActuelle() +"), donc coupure du run en cours.");
                    gestionNuit(p, fin, debut, nbPassages);
                    cancel();
                    return;
                }

                if (p.getWorld().getFullTime() % 24000 + incrementationMinecraft > 23999){
                    p.getWorld().setTime(p.getWorld().getFullTime() % 24000 + incrementationMinecraft - 23999);
                    System.out.println("[infos] : temps actuel + incr > 23999 dans le if ");
                }
                else
                {
                    p.getWorld().setTime(p.getWorld().getFullTime() % 24000 + incrementationMinecraft);
                    System.out.println("Soleil déplacé de " + incrementationMinecraft + ", pos actuelle : " + p.getWorld().getFullTime() % 24000);
                }

                Bukkit.broadcastMessage("++ pos soleil de " + incrementationMinecraft + ".");
            }
        };

        tache.runTaskTimer(this.tinnu, 0, incrementationVraiHeure * 20);
        System.out.println("[infos] : passage juste après le runTaskTimer dans gestionJour");
        System.out.println("[infos] : fin de la fonction gestionJour");
    }

    private void gestionNuit(Player p, LocalTime debut, LocalTime fin, int nbPassages) {

        System.out.println("\n\n[Temp] : Lancement de la fonction gestionNuit\n\n");

        // Verification des valeurs passées à la fonction
        if (!debut.isAfter(LocalTime.of(12,00)) || !fin.isBefore(LocalTime.of(12,00))){
            System.out.println("[erreur] : erreur dans les valeurs passées à gestionNuit, arret de la fonction.");
            return;
        }

        // On initialise la position actuelle de la lune
        int posLuneActuelle = debutNuit;
        // On initialise la lune au début de la nuit
        p.getWorld().setTime(posLuneActuelle);
        System.out.println("[infos] : lune à été placée à le soir, pos actuelle : " + posLuneActuelle + "...");

        // On vérifie qu'on récupère bien l'heure actuelle
        if (getHeureActuelle() == null) {
            System.out.println("[Erreur] : Heure factice est nulle ");
            return;
        }

        // On vérifie que la valeur de début n'est pas nulle
        if (debut == null) {
            System.out.println("[Erreur] : debut est null ");
            return;
        }

        // On vérifie que la valeur de fin n'est ps nulle
        if (fin == null) {
            System.out.println("[Erreur] : fin est null ");
            return;
        }

        // On s'assure que le cycle Jour / nuit est désactivé
        p.getWorld().setGameRuleValue("doDaylightCycle", "false");

        // Nombre total de minutes constituant la journée à grer
        int minutesTotales = setMinutesTotales(debut, fin);

        // On définit chaque combien de SECONDES on fait bouger la lune
        double valtemp = (double) minutesTotales / nbPassages * 60;
        int incrementationVraiHeure = (int)valtemp;
        System.out.println("[infos] : Incrementation vrai heure : "+ incrementationVraiHeure + " secondes et valtemp : " + valtemp + "secondes");

        // On définit de combien on fait bouger la lune à chaque fois
        int incrementationMinecraft = this.tempsJour / nbPassages;

        // On récupère le temps à rattraper depuis le début du soir (en minutes)
        double tempsARattraper = setTempsARattraper(debut, getHeureActuelle(),2);

        double cpt = 0;
        if (tempsARattraper > 0 ) {
            System.out.println("[infos] :heure théorique de début de la fonction : " + debut + "h");
            System.out.println("[infos] : heure de début de la fonction : " + getHeureActuelle() +"h");
            System.out.println("[infos] : Le temps à rattraper est donc de : " + tempsARattraper);

            cpt = incrementationMinecraft * ((tempsARattraper / minutesTotales) * nbPassages);
        }

        posLuneActuelle = posLuneActuelle + (int)cpt;
        if (posLuneActuelle > 23999){
            posLuneActuelle = posLuneActuelle - 23999;
        }

        p.getWorld().setTime(posLuneActuelle);
        System.out.println("[infos] : La lune rattrape son retard de " + cpt + " et est mtn à " + posLuneActuelle + ".");

        System.out.print("\n\n\n\n[Temp] : Bilan des variables : ");
        System.out.println("\t - debut : " + debut);
        System.out.println("\t - fin : " + fin);
        System.out.println("\t - nbRepetitions : " + nbPassages);
        System.out.println("\t - minutes totales : " + minutesTotales);
        System.out.println("\t - Heure actuelle : " + getHeureActuelle());
        System.out.println("\t - Temps à rattraper : " + tempsARattraper);
        System.out.println("\t - incrementation vrai heure : " + incrementationVraiHeure + " secondes");
        System.out.println("\t - incrementationMinecraft : " + incrementationMinecraft);
        System.out.println("\t - att. debut jour : " + debutJour);
        System.out.println("\t - att. debut nuit : " + debutNuit);
        System.out.println("\t - temps jour  : " + tempsJour);
        System.out.println("\t - temps nuit  : " + tempsNuit);
        System.out.println("\n\n\n\n");


        System.out.println("[Infos: passsage juste avant la decla. du runnable dans gestion nuit ");
        BukkitRunnable tache = new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("[infos] : Passage dans un run...");
                System.out.println("[infos] : act  : " + p.getWorld().getFullTime() % 24000 + " debut jour : " + debutJour);
                int val = 0;

                if (!enMarche) {
                    p.getWorld().setTime(debutJour);
                    System.out.println("[infos] : arret du run en cours car !enMarche");
                    cancel();
                    allumerHorloge();
                    return;
                }

                if (fin.isBefore(getHeureActuelle()) && getHeureActuelle().isBefore(LocalTime.of(12,00))){
                    p.getWorld().setTime(debutJour);
                    System.out.println("La fin de nuit ("+ fin + ") est avant l'heure actuelle("+ getHeureActuelle() +"), donc coupure du run en cours.");
                    gestionJour(p, fin, debut, nbPassages);
                    cancel();
                    return;
                }

                if (p.getWorld().getFullTime() % 24000 + incrementationMinecraft > 23999){
                    p.getWorld().setTime(p.getWorld().getFullTime() % 24000 + incrementationMinecraft - 23999);
                    System.out.println("[infos] : temps actuel + incr > 23999 dans le if ");
                }
                else
                {
                    p.getWorld().setTime(p.getWorld().getFullTime() % 24000 + incrementationMinecraft);
                    System.out.println("Lune déplacée de " + incrementationMinecraft + ", pos actuelle : " + p.getWorld().getFullTime() % 24000);
                }
                Bukkit.broadcastMessage("++ pos lune de " + incrementationMinecraft + ".");


            }
        };
        tache.runTaskTimer(this.tinnu, 0, incrementationVraiHeure * 20);
        System.out.println("[infos] : passage juste après le runTaskTimer dans gestionNuit");
        System.out.println("[infos] : fin de la fonction gestionNuit");
    }

    private LocalTime getHeureActuelle() {
        return LocalTime.now();
        //return heureActuelleFactice;
    }

    private boolean initialisationCommandesBasiques(CommandSender sender, Command cmd, String msg, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tinnu")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("jour")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.getWorld().setTime(6000);
                    player.sendMessage("[tinnu] : Il fait jour maintenant !");

                    return true;
                }
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("nuit")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.getWorld().setTime(18000);
                    player.sendMessage("[tinnu] : Il fait jour nuit !");

                    return true;
                }
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("soir")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.getWorld().setTime(12000);
                    player.sendMessage("[tinnu] : Il fait jour maintenant !");

                    return true;
                }
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("aube")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.getWorld().setTime(0);
                    player.sendMessage("[tinnu] : C'est le matin maintenant !");

                    return true;
                }
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("allumerhorloge")) {
                if (sender instanceof Player) {
                    this.allumerHorloge();
                    sender.sendMessage("[tinnu] : Horloge allumée !");

                    return true;
                }
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("eteindrehorloge")) {
                if (sender instanceof Player) {
                    this.eteindreHorloge();
                    sender.sendMessage("[tinnu] : Horloge éteinte ! ");

                    return true;
                }
            }

            if (cmd.getName().equalsIgnoreCase("tinnu")){
            if (args.length == 3 && args[0].equalsIgnoreCase("setHeureActuelle")) {
                this.heureActuelleFactice = LocalTime.of(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
                sender.sendMessage("L'heure actuelle fictive est maintenant " + args[1]+"h" + args[2]);
                return true;
            }
        }

        return false;
    }

    private void allumerHorloge() {
        this.enMarche = true;
    }

    private void eteindreHorloge() {
        this.enMarche = false;
    }

        public Horloge(Tinnu tinnu) {

        this.tinnu = tinnu;
        this.heureActuelleFactice = LocalTime.of(9, 35);
        this.enMarche = true;
        /*this.debutJour = 23000;
        this.debutNuit = 13300;
        this.tempsJour = 13300 + 999;
        this.tempsNuit = 23000 - 13300;*/

        this.debutJour = 0;
        this.debutNuit = 12000;
        this.tempsJour = 12000;
        this.tempsNuit = 12000;
    }
}
