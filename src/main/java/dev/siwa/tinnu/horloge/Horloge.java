package dev.siwa.tinnu.horloge;
import dev.siwa.tinnu.Tinnu;

import org.bukkit.World;
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
    private int debutJour;
    private int debutNuit;
    private int tempsNuit = -1;
    private int tempsJour = -1;

    private LocalTime heureAube;
    private LocalTime heureSoir;
    public Tinnu tinnu ;


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        // init. des commandes jour, nuit, soir et aube.
        if (initialisationCommandes(sender, cmd, msg, args)){
            return true;
        }
        //System.out.println("\n\n[temp] : Passage après init. Commandes basiques...\n\n");

        sender.sendMessage("[Tinnu] : Aucune commande correspondante trouvée, il faut réfléchir un peu...");
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

    private void gestionJour(World w, LocalTime debut, LocalTime fin, int nbPassages) {

        //System.out.println("\n\n[Temp] : Lancement de la fonction gestionJour\n\n");

        // Verification des heures données
        if (!debut.isBefore(LocalTime.of(12,00)) || !fin.isAfter(LocalTime.of(12,00))){
            System.out.println("[Tinnu] : Erreur dans les valeurs données à la fonction gestionJour, arret de la fonction.");
            return;
        }

        // On initialise la position actuelle du soleil au début de la journée
        int posSoleilActuelle = debutJour;

        // On initialise le soleil au début de la journée
        w.setTime(posSoleilActuelle);
        //System.out.println("[infos] : soleil a ete place à l'aube, pos actuelle : " + posSoleilActuelle + "...");

        // On vérifie qu'on récupère bien l'heure actuelle 
        if (tinnu.getHeureActuelle() == null) {
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
        w.setGameRuleValue("doDaylightCycle", "false");

        // Nombre total de minutes constituant la journée à gérer
        int minutesTotales = setMinutesTotales(debut, fin);
        if (minutesTotales == -1){
            System.out.println("[Erreur] : Soucis dans le calcul des minutes totales, fin de la fonction.");
            return;
        }

        // On définit chaque combien de SECONDES on fait bouger le soleil
        double valtemp = (double) minutesTotales / nbPassages * 60;
        int incrementationVraiHeure = (int)valtemp;
        //System.out.println("[infos] : Incrementation vrai heure : "+ incrementationVraiHeure + " secondes et valtemp : " + valtemp + "secondes");

        // On définit de combien on fait bouger le soleil à chaque fois 
        int incrementationMinecraft = this.tempsJour / nbPassages;

        // On récupère le temps à rattraper depuis le début du jour (en minutes)
        double tempsARattraper = setTempsARattraper(debut, tinnu.getHeureActuelle(),1);

        double cpt = 0;
        if (tempsARattraper > 0 ) {
            /*System.out.println("[infos] :heure théorique de début de la fonction : " + debut + "h");
            System.out.println("[infos] : heure réeelle de début de la fonction : " + tinnu.getHeureActuelle() +"h");
            System.out.println("[infos] : Le temps à rattraper est donc de : " + tempsARattraper + "minutes.");*/

            cpt = incrementationMinecraft * ((tempsARattraper / minutesTotales) * nbPassages);

            /*System.out.println("[infos] :((tempsARattraper / minutesTotales) * nbPassages) -->  " + ((tempsARattraper / minutesTotales) * nbPassages));
            System.out.println("[infos] : incrementation mc : " + incrementationMinecraft);
            System.out.println("[infos] : (int)cpt (cpt : " + cpt + " ) : " + (int)cpt);*/
        }

        posSoleilActuelle = posSoleilActuelle + (int)cpt;
        //System.out.println("[infos] : pos actuelle : " + posSoleilActuelle);

        if (posSoleilActuelle > 23999){
            posSoleilActuelle = posSoleilActuelle % 24000;
            //System.out.println("[infos] : pos actuelle < 23999 donc remise a niveau % 24000 ; pos actuelle mtn : " + posSoleilActuelle);
        }

        w.setTime(posSoleilActuelle);
        //System.out.println("[infos] : Le soleil rattrape son retard de " + cpt + " et est mtn a " + posSoleilActuelle + ".");

        /*System.out.print("\n\n\n\n[Temp] : Bilan des variables : ");
        System.out.println("\t - debut : " + debut);
        System.out.println("\t - fin : " + fin);
        System.out.println("\t - nbRepetitions : " + nbPassages);
        System.out.println("\t - minutes totales : " + minutesTotales);
        System.out.println("\t - Heure actuelle : " + tinnu.getHeureActuelle());
        System.out.println("\t - Temps à rattraper : " + tempsARattraper);
        System.out.println("\t - incrementation vrai heure : " + incrementationVraiHeure + " secondes");
        System.out.println("\t - incrementationMinecraft : " + incrementationMinecraft);
        System.out.println("\t - att. debut jour : " + debutJour);
        System.out.println("\t - att. debut nuit : " + debutNuit);
        System.out.println("\t - temps jour  : " + tempsJour);
        System.out.println("\t - temps nuit  : " + tempsNuit);
        System.out.println("\n\n\n\n");*/


        //System.out.println("[Infos: passsage juste avant la decla. du runnable dans gestion jour ");
        BukkitRunnable tache = new BukkitRunnable() {
            @Override
            public void run() {
                /*System.out.println("[infos] : Passage dans un run...");
                System.out.println("[infos] : act  : " + p.getWorld().getFullTime() % 24000 + " debut nuit : " + debutNuit);*/

                if (!enMarche) {
                    w.setTime(debutNuit);
                    System.out.println("[infos] : arret du run en cours car !enMarche");
                    cancel();
                    allumerHorloge();
                    return;
                }

                if (fin.isBefore(tinnu.getHeureActuelle())){
                    w.setTime(debutNuit);
                    //System.out.println("La fin de journée (" + fin + ") est avant l'heure actuelle("+ tinnu.getHeureActuelle() +"), donc coupure du run en cours.");
                    gestionNuit(w, fin, debut, nbPassages);
                    cancel();
                    return;
                }

                if (w.getFullTime() % 24000 + incrementationMinecraft > 23999){
                    w.setTime(w.getFullTime() % 24000 + incrementationMinecraft - 23999);
                    //System.out.println("[infos] : temps actuel + incr > 23999 dans le if ");
                }
                else
                {
                    w.setTime(w.getFullTime() % 24000 + incrementationMinecraft);
                    //System.out.println("Soleil déplacé de " + incrementationMinecraft + ", pos actuelle : " + p.getWorld().getFullTime() % 24000);
                }

                //Bukkit.broadcastMessage("++ pos soleil de " + incrementationMinecraft + ".");
            }
        };

        tache.runTaskTimer(this.tinnu, 0, incrementationVraiHeure * 20);
        /*System.out.println("[infos] : passage juste après le runTaskTimer dans gestionJour");
        System.out.println("[infos] : fin de la fonction gestionJour");*/
    }

    private void gestionNuit(World w, LocalTime debut, LocalTime fin, int nbPassages) {

        //System.out.println("\n\n[Temp] : Lancement de la fonction gestionNuit\n\n");

        // Verification des valeurs passées à la fonction
        if (!debut.isAfter(LocalTime.of(12,00)) || !fin.isBefore(LocalTime.of(12,00))){
            System.out.println("[Tinnu] : Erreur dans les valeurs passées à gestionNuit, arret de la fonction.");
            return;
        }

        // On initialise la position actuelle de la lune
        int posLuneActuelle = debutNuit;
        // On initialise la lune au début de la nuit
        w.setTime(posLuneActuelle);
        //System.out.println("[infos] : lune à été placée à le soir, pos actuelle : " + posLuneActuelle + "...");

        // On vérifie qu'on récupère bien l'heure actuelle
        if (tinnu.getHeureActuelle() == null) {
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
        w.setGameRuleValue("doDaylightCycle", "false");

        // Nombre total de minutes constituant la journée à grer
        int minutesTotales = setMinutesTotales(debut, fin);

        // On définit chaque combien de SECONDES on fait bouger la lune
        double valtemp = (double) minutesTotales / nbPassages * 60;
        int incrementationVraiHeure = (int)valtemp;
        //System.out.println("[infos] : Incrementation vrai heure : "+ incrementationVraiHeure + " secondes et valtemp : " + valtemp + "secondes");

        // On définit de combien on fait bouger la lune à chaque fois
        int incrementationMinecraft = this.tempsJour / nbPassages;

        // On récupère le temps à rattraper depuis le début du soir (en minutes)
        double tempsARattraper = setTempsARattraper(debut, tinnu.getHeureActuelle(),2);

        double cpt = 0;
        if (tempsARattraper > 0 ) {
            /*System.out.println("[infos] :heure théorique de début de la fonction : " + debut + "h");
            System.out.println("[infos] : heure de début de la fonction : " + tinnu.getHeureActuelle() +"h");
            System.out.println("[infos] : Le temps à rattraper est donc de : " + tempsARattraper);*/

            cpt = incrementationMinecraft * ((tempsARattraper / minutesTotales) * nbPassages);
        }

        posLuneActuelle = posLuneActuelle + (int)cpt;
        if (posLuneActuelle > 23999){
            posLuneActuelle = posLuneActuelle - 23999;
        }

        w.setTime(posLuneActuelle);
        /*System.out.println("[infos] : La lune rattrape son retard de " + cpt + " et est mtn à " + posLuneActuelle + ".");

        System.out.print("\n\n\n\n[Temp] : Bilan des variables : ");
        System.out.println("\t - debut : " + debut);
        System.out.println("\t - fin : " + fin);
        System.out.println("\t - nbRepetitions : " + nbPassages);
        System.out.println("\t - minutes totales : " + minutesTotales);
        System.out.println("\t - Heure actuelle : " + tinnu.getHeureActuelle());
        System.out.println("\t - Temps à rattraper : " + tempsARattraper);
        System.out.println("\t - incrementation vrai heure : " + incrementationVraiHeure + " secondes");
        System.out.println("\t - incrementationMinecraft : " + incrementationMinecraft);
        System.out.println("\t - att. debut jour : " + debutJour);
        System.out.println("\t - att. debut nuit : " + debutNuit);
        System.out.println("\t - temps jour  : " + tempsJour);
        System.out.println("\t - temps nuit  : " + tempsNuit);
        System.out.println("\n\n\n\n");*/


        //System.out.println("[Infos: passsage juste avant la decla. du runnable dans gestion nuit ");
        BukkitRunnable tache = new BukkitRunnable() {
            @Override
            public void run() {
                /*System.out.println("[infos] : Passage dans un run...");
                System.out.println("[infos] : act  : " + p.getWorld().getFullTime() % 24000 + " debut jour : " + debutJour);*/
                int val = 0;

                if (!enMarche) {
                    w.setTime(debutJour);
                    System.out.println("[infos] : arret du run en cours car !enMarche");
                    cancel();
                    allumerHorloge();
                    return;
                }

                if (fin.isBefore(tinnu.getHeureActuelle()) && tinnu.getHeureActuelle().isBefore(LocalTime.of(12,00))){
                    w.setTime(debutJour);
                    //System.out.println("La fin de nuit ("+ fin + ") est avant l'heure actuelle("+ tinnu.getHeureActuelle() +"), donc coupure du run en cours.");
                    gestionJour(w, fin, debut, nbPassages);
                    cancel();
                    return;
                }

                if (w.getFullTime() % 24000 + incrementationMinecraft > 23999){
                    w.setTime(w.getFullTime() % 24000 + incrementationMinecraft - 23999);
                    //System.out.println("[infos] : temps actuel + incr > 23999 dans le if ");
                }
                else
                {
                    w.setTime(w.getFullTime() % 24000 + incrementationMinecraft);
                    //System.out.println("Lune déplacée de " + incrementationMinecraft + ", pos actuelle : " + p.getWorld().getFullTime() % 24000);
                }
                //Bukkit.broadcastMessage("++ pos lune de " + incrementationMinecraft + ".");


            }
        };
        tache.runTaskTimer(this.tinnu, 0, incrementationVraiHeure * 20);
        /*System.out.println("[infos] : passage juste après le runTaskTimer dans gestionNuit");
        System.out.println("[infos] : fin de la fonction gestionNuit");*/
    }

    private boolean initialisationCommandes(CommandSender sender, Command cmd, String msg, String[] args) {
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

        if (cmd.getName().equalsIgnoreCase("tinnu")) {
            if (args.length == 6 && args[0].equalsIgnoreCase("synchronisationJourMannuelle")) {
                LocalTime debut = LocalTime.of(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
                LocalTime fin = LocalTime.of(Integer.valueOf(args[3]), Integer.valueOf(args[4]));
                int nbPassages = Integer.valueOf(args[5]);
                System.out.println("[Infos] : Commande synchroJourMannuelle bien trouvée + bons arguments.");

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.sendMessage("[tinnu] : Vous lancez manuellement la synchronisation du jour avec " + args[1] + "h"+ args[2] + " en heure de debut du matin, " + args[3] + "h" + args[4] + " en heure de fin de la soiree et " + nbPassages + " changement de position du soleil dans la journee.");
                    gestionJour(player.getWorld(), debut, fin, nbPassages);
                    return true;
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("tinnu")) {
            if (args.length == 6 && args[0].equalsIgnoreCase("synchronisationNuitMannuelle")) {
                LocalTime debut = LocalTime.of(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
                LocalTime fin = LocalTime.of(Integer.valueOf(args[3]), Integer.valueOf(args[4]));
                int nbPassages = Integer.valueOf(args[5]);
                System.out.println("[Infos] : Commande synchroJourMannuelle bien trouvée + bons arguments.");

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.sendMessage("[tinnu] : Vous lancez manuellement la synchronisation du jour avec " + args[1] + "h"+ args[2] + " en heure de debut du matin, " + args[3] + "h" + args[4] + " en heure de fin de la soiree et " + nbPassages + " changement de position du soleil dans la journee.");
                    gestionNuit(player.getWorld(), debut, fin, nbPassages);
                    return true;
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("tinnu")){
            if (args.length == 3 && args[0].equalsIgnoreCase("setHeureActuelle")) {
                this.heureActuelleFactice = LocalTime.of(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
                sender.sendMessage("L'heure actuelle fictive est maintenant " + args[1]+"h" + args[2]);
                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("tinnu")) {
            if (args.length == 6 && args[0].equalsIgnoreCase("synchroJourNuit")) {

                LocalTime debut = LocalTime.of(Integer.valueOf(args[1]), Integer.valueOf(args[2]));
                LocalTime fin = LocalTime.of(Integer.valueOf(args[3]), Integer.valueOf(args[4]));
                int nbPassages = Integer.valueOf(args[5]);

                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    if (debut.isBefore(tinnu.getHeureActuelle()) && fin.isAfter(tinnu.getHeureActuelle())){
                        gestionJour(player.getWorld(), debut, fin, nbPassages);
                    }
                    else
                    {
                        gestionNuit(player.getWorld(), fin, debut,  nbPassages);
                    }
                    return true;
                }
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("synchro")){
                if (args[1].equalsIgnoreCase("estiu")){
                    if (sender instanceof Player){
                        Player player = (Player) sender;
                        int nbPassages = 1000;

                        if (this.heureAube.isBefore(tinnu.getHeureActuelle()) && this.heureSoir.isAfter(tinnu.getHeureActuelle())){
                            gestionJour(player.getWorld(), this.heureAube, this.heureSoir, nbPassages);
                        }
                        else
                        {
                            gestionNuit(player.getWorld(), this.heureSoir, this.heureAube,  nbPassages);
                        }

                        player.sendMessage("[Tinnu] : Lancement de la synchronisation Jour/Nuit avec les valeurs par défaut ( aube : "
                                + this.heureAube + " ; soir : " + this.heureSoir + " ; nbPassages : " + nbPassages);
                        return true;
                    }
                }

            }

            if (args.length == 7 && args[0].equalsIgnoreCase("synchro")){
                if (args[1].equalsIgnoreCase("estiu")){
                    if (sender instanceof Player){
                        Player player = (Player) sender;
                        this.heureAube = LocalTime.of(Integer.valueOf(args[2]), Integer.valueOf(args[3]));
                        this.heureSoir = LocalTime.of(Integer.valueOf(args[4]), Integer.valueOf(args[5]));
                        int nbPassages = Integer.valueOf(args[6]);

                        if (this.heureAube.isBefore(tinnu.getHeureActuelle()) && this.heureSoir.isAfter(tinnu.getHeureActuelle())){
                            gestionJour(player.getWorld(), this.heureAube, this.heureSoir, nbPassages);
                        }
                        else
                        {
                            gestionNuit(player.getWorld(), this.heureSoir, this.heureAube,  nbPassages);
                        }

                        player.sendMessage("[Tinnu] : Lancement de la synchronisation Jour/Nuit avec les valeurs ( aube : "
                                + this.heureAube + " ; soir : " + this.heureSoir + " ; nbPassages : " + nbPassages);
                        return true;
                    }
                }

            }

            if (args.length == 1 && args[0].equalsIgnoreCase("stopsynchro")){
                if (sender instanceof Player){
                    Player p = (Player) sender;
                    this.eteindreHorloge();
                    p.sendMessage("[Tinnu] : L'horloge de synchronisation a ete eteinte.");
                    return true;
                }
            }

            if (args.length == 4 && args[0].equalsIgnoreCase("set")){
                if (args[1].equalsIgnoreCase("aube")){
                    if (sender instanceof Player){
                        Player p = (Player) sender;
                        this.setHeureAube(LocalTime.of(Integer.valueOf(args[2]), Integer.valueOf(args[3])));
                        p.sendMessage("[Tinnu] : L'aube a été fixée a : " + this.heureAube);
                        return true;
                    }
                }

                if (args[1].equalsIgnoreCase("soir")){
                    if (sender instanceof Player){
                        Player p = (Player) sender;
                        this.setheureSoir(LocalTime.of(Integer.valueOf(args[2]), Integer.valueOf(args[3])));
                        p.sendMessage("[Tinnu] : Le soir a été fixé a : " + this.heureSoir);
                        return true;
                    }
                }
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("set")){

                if (Integer.valueOf(args[2]) > 23999){
                    sender.sendMessage("[Tinnu] : La valeur est supérieure a 23999, echec.");
                    return false;
                }

                if (args[1].equalsIgnoreCase("aubemc")){

                    if (sender instanceof Player){
                        Player p = (Player) sender;
                        this.setDebutJour(Integer.valueOf(args[2]));
                        p.sendMessage("[Tinnu] : Le matin dans le jeu est mtn a : " + this.debutJour);
                        return true;
                    }
                }

                if (args[1].equalsIgnoreCase("soirmc")){
                    if (sender instanceof Player){
                        Player p = (Player) sender;
                        this.setDebutNuit(Integer.valueOf(args[2]));
                        p.sendMessage("[Tinnu] : Le soir dans le jeu est mtn a  " + this.debutNuit);
                        return true;
                    }
                }
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("affichagevaleurs")){
                if (sender instanceof Player){
                    Player p = (Player) sender;
                    this.affichageValeurs(p);
                    return true;
                }
            }
        }

        return false;
    }

    private void allumerHorloge() {
        this.enMarche = true;
    }

    private void affichageValeurs(Player p) {
        p.sendMessage("[Tinnu] : Aube : "+this.heureAube+" ;  Soir : "+this.heureSoir+" ;  Aube-mc : "+this.debutJour+" ;   Soir-mc : "+this.debutNuit);
    }

    private void eteindreHorloge() {
        this.enMarche = false;
    }

    private void setDebutJour(int val) {
        this.debutJour = val;

        if (debutJour > debutNuit){
            this.tempsJour = 24000 - debutJour + debutNuit;
        }
        if (debutJour < debutNuit){
            this.tempsJour = debutNuit - debutJour;
        }

        this.tempsNuit = 24000 - tempsJour;
        if (this.tempsJour == -1 || this.tempsNuit == -1){
            System.out.println("Soucis dans le calcul des temps de jour / nuit.");
        }
    }

    private void setDebutNuit(int val) {
        this.debutNuit = val;

        if (debutJour > debutNuit){
            this.tempsJour = 24000 - debutJour + debutNuit;
        }
        if (debutJour < debutNuit){
            this.tempsJour = debutNuit - debutJour;
        }

        this.tempsNuit = 24000 - tempsJour;
        if (this.tempsJour == -1 || this.tempsNuit == -1){
            System.out.println("Soucis dans le calcul des temps de jour / nuit.");
        }
    }

    private void setheureSoir(LocalTime val) {
        this.heureSoir = val;
    }

    public void lancement(World w) {
        if (this.heureAube.isBefore(tinnu.getHeureActuelle()) && this.heureSoir.isAfter(tinnu.getHeureActuelle())){
            gestionJour(w, this.heureAube, this.heureSoir, 1000);
        }
        else
        {
            gestionNuit(w, this.heureSoir, this.heureAube,  1000);
        }
        System.out.println("Lancement de la synchronisation jour / nuit");
    }

    private void setHeureAube(LocalTime val) {
        this.heureAube = val;
    }

        public Horloge(Tinnu tinnu) {

        this.tinnu = tinnu;
        this.heureActuelleFactice = LocalTime.of(9, 35);
        this.enMarche = true;

        this.debutJour = 0;
        this.debutNuit = 12000;
        this.heureAube = LocalTime.of(6,30);
        this.heureSoir = LocalTime.of(21,30);


        if (debutJour > debutNuit){
            this.tempsJour = 24000 - debutJour + debutNuit;
        }
        if (debutJour < debutNuit){
            this.tempsJour = debutNuit - debutJour;
        }

        this.tempsNuit = 24000 - tempsJour;
        if (this.tempsJour == -1 || this.tempsNuit == -1){
            System.out.println("Soucis dans le calcul des temps de jour / nuit.");
        }

    }
}
