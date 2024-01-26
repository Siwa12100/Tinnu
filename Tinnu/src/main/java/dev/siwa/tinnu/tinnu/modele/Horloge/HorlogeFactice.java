package dev.siwa.tinnu.tinnu.modele.Horloge;

import java.time.LocalTime;

public class HorlogeFactice implements IHorloge {

    public HorlogeFactice() {

    }

    @Override
    public LocalTime getHeureCourante() {
        return LocalTime.of(12, 00, 00);
    }

    @Override
    public LocalTime getHeureLeveSoleil() {
        return LocalTime.of(7, 00, 00);
    }

    @Override
    public LocalTime getHeureCoucheSoleil() {
        return LocalTime.of(21, 00, 00);
    }
}
