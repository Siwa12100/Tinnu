package dev.siwa.tinnu.tinnu.modele.Horloge;

import dev.siwa.tinnu.tinnu.config.TinnuConfig;
import jdk.jshell.execution.LoaderDelegate;

import java.time.LocalTime;

public class Horloge implements IHorloge {

    public Horloge() {

    }
    @Override
    public LocalTime getHeureCourante() {
        return LocalTime.now();
    }

    @Override
    public LocalTime getHeureLeveSoleil() {
        return TinnuConfig.getHeureLeveSoleil();
    }

    @Override
    public LocalTime getHeureCoucheSoleil() {
        return TinnuConfig.getHeureCoucheSoleil();
    }
}
