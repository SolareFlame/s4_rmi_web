package activeRecord;

public class ServeurActionNonPermiseException extends Exception {
    public ServeurActionNonPermiseException() {
        super("Serveur : Vous n'avez pas les droits pour effectuer cette action");
    }
}
