package database;

public class ServeurActionNonPermiseException extends Exception {
    public ServeurActionNonPermiseException() {
        super("ServiceDatabase : Vous n'avez pas les droits pour effectuer cette action");
    }
}
