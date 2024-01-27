package dev.siwa.tinnu.tinnu.modele.Horloge;

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
        return LocalTime.of(01, 49);
    }

    @Override
    public LocalTime getHeureCoucheSoleil() {
        return LocalTime.of(01, 51);
    }
}
