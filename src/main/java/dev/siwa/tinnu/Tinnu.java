package dev.siwa.tinnu;
import dev.siwa.tinnu.horloge.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;

import java.time.LocalTime;
import java.util.Objects;

public final class Tinnu extends JavaPlugin {

    public static Tinnu tinnu;

    @Override
    public void onEnable() {
        tinnu = this;

        Horloge h =new Horloge(tinnu);

        for (World w : Bukkit.getWorlds()){
            System.out.println("\n\n - " + w.getName());
        }

        System.out.println("\n\n ---> Lancement du plugin Tinnu.\n\n");

        for (World w : Bukkit.getWorlds()){
            if (w.getName().equals("survie")){
                h.lancement(w);
                System.out.println(" ---> Lancement de la synchro jour / nuit dans le monde survie");
            }

            if (w.getName().equals("donjons")){
                h.lancement(w);
                System.out.println("---> Lancement de la synchro jour / nuit dans le monde donjons");
            }

            if (w.getName().equals("itayas1")){
                h.lancement(w);
                System.out.println("---> Lancement de la synchro jour / nuit dans le monde Itayas1");
            }

            if (w.getName().equals("eventides1")){
                h.lancement(w);
                System.out.println("---> Lancement de la synchro jour / nuit dans le monde eventides1");
            }
        }

        // Pour l'Agora
        /*for (World w : Bukkit.getWorlds()){
            if (w.getName().equals("world")){
                h.lancement(w);
                System.out.println(" ---> Lancement de la synchro jour / nuit dans le monde de l'Agora");
            }

            if (w.getName().equals("antonio_world")){
                h.lancement(w);
                System.out.println("---> Lancement de la synchro jour / nuit dans le monde de Antonio");
            }
        }*/

        initialisationCommandes(h);
        System.out.println("---> Commandes de tinnu initialisées.");
        System.out.println("---> Fin du OnEnable de Tinnu");
    }

    @Override
    public void onDisable() {
        System.out.println("\n\n ---> Extinction du plugin Tinnu.\n\n");
    }

    private void initialisationCommandes(Horloge h) {
        Objects.requireNonNull(getCommand("tinnu")).setExecutor(h);
        //getCommand("tn").setExecutor(new Horloge());
    }

    public LocalTime getHeureActuelle() {
        return LocalTime.now();
        //return heureActuelleFactice;
    }

    public Tinnu() {

    }
}
