package dev.siwa.tinnu;
import dev.siwa.tinnu.horloge.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Objects;

public final class Tinnu extends JavaPlugin {

    public static Tinnu tinnu;

    @Override
    public void onEnable() {
        tinnu = this;
        System.out.println("\n\n ---> Lancement du plugin Tinnu.\n\n");
        initialisationCommandes();
    }

    @Override
    public void onDisable() {
        System.out.println("\n\n ---> Extinction du plugin Tinnu.\n\n");
    }

    private void initialisationCommandes() {
        Objects.requireNonNull(getCommand("tinnu")).setExecutor(new Horloge(this));
        //getCommand("tn").setExecutor(new Horloge());
    }

    public Tinnu() {

    }
}
