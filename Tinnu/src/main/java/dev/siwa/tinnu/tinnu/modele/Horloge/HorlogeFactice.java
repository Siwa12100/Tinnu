package dev.siwa.tinnu.tinnu.modele.Horloge;

import java.time.LocalTime;

public class HorlogeFactice implements IHorloge {

    public HorlogeFactice() {

    }

    @Override
    public LocalTime getHeureCourante() {
        return LocalTime.of(20, 0, 00);
    }

    @Override
    public LocalTime getHeureLeveSoleil() {
        return LocalTime.of(19, 59, 00);
    }

    @Override
    public LocalTime getHeureCoucheSoleil() {
        return LocalTime.of(20, 2, 00);
    }
}
