package dev.siwa.tinnu.tinnu;

import dev.siwa.tinnu.tinnu.affichage.Afficheur;
import dev.siwa.tinnu.tinnu.modele.Horloge.Horloge;
import dev.siwa.tinnu.tinnu.modele.Horloge.HorlogeFactice;
import dev.siwa.tinnu.tinnu.modele.cadran.Cadran;
import org.bukkit.plugin.java.JavaPlugin;

public final class Tinnu extends JavaPlugin {

    private static Tinnu instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Afficheur.activerModeDebug();
        Afficheur.afficherDebug("Lancement du OnEnable");
        Tinnu.instance = this;



        Cadran cadran = new Cadran(new Horloge());
        cadran.ajouterMonde(getServer().getWorld("world"));
        cadran.lancerSynchronisation();

        Afficheur.afficherDebug("Fin du OnEnable");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        Afficheur.afficherDebug("Fin du onDisable");
    }

    public static Tinnu getInstance() {
        return Tinnu.instance;
    }
}
