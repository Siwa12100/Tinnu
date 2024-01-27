package dev.siwa.tinnu.tinnu;

import dev.siwa.tinnu.tinnu.affichage.Afficheur;
import dev.siwa.tinnu.tinnu.config.TinnuConfig;
import dev.siwa.tinnu.tinnu.modele.Horloge.Horloge;
import dev.siwa.tinnu.tinnu.modele.Horloge.HorlogeFactice;
import dev.siwa.tinnu.tinnu.modele.cadran.Cadran;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Tinnu extends JavaPlugin {

    private static Tinnu instance;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        //Afficheur.activerModeDebug();
        Afficheur.afficherDebug("Lancement du OnEnable");
        Tinnu.instance = this;
        this.loadConfigFile();
        TinnuConfig.chargerConfig(this.config);
        System.out.println(TinnuConfig.afficherConfig());

        if (TinnuConfig.isTinnuActif()) {
            Cadran cadran = new Cadran(new Horloge());
            cadran.ajouterMonde(getServer().getWorld("world"));
            cadran.lancerSynchronisation();
        }

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

    private void loadConfigFile() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }
}
