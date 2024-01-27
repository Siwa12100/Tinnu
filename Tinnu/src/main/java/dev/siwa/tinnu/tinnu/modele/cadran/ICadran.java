package dev.siwa.tinnu.tinnu.modele.cadran;

import org.bukkit.World;

import java.util.List;

public interface ICadran {
    void lancerSynchronisation();
    void setListeMondes(List<World> lesMondes);
    void ajouterMonde(World nouveauMonde);
}
