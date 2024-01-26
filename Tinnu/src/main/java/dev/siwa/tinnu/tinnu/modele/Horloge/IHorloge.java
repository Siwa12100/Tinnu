package dev.siwa.tinnu.tinnu.modele.Horloge;

import java.time.LocalTime;

public interface IHorloge {

    LocalTime getHeureCourante();
    LocalTime getHeureLeveSoleil();
    LocalTime getHeureCoucheSoleil();
}
