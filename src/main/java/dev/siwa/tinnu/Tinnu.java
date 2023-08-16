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
        Horloge h =new Horloge(tinnu);
        initialisationCommandes(h);
    }

    @Override
    public void onDisable() }

    private void initialisationCommandes(Horloge h) {
        Objects.requireNonNull(getCommand("tinnu")).setExecutor(h);
    }

    public LocalTime getHeureActuelle() {
        return LocalTime.now();
    }

    public Tinnu() {
        this.tinnu = this;
    }
}
