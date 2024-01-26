package dev.siwa.tinnu.tinnu.affichage;

public class Afficheur {

    private static boolean modeDebugActif = false;

    public static void activerModeDebug() {
        Afficheur.modeDebugActif = true;
    }

    public static void desactiverModeDebug() {
        Afficheur.modeDebugActif = false;
    }

    public static void afficherInfo(String msg) {
        System.out.println("\n[Tinnu] : " + msg + "\n");
    }

    public static void afficherDebug(String msg) {
        if (Afficheur.modeDebugActif) {
            System.out.println("\n[Tinnu debug] : " + msg + "\n");
        }
    }
}
