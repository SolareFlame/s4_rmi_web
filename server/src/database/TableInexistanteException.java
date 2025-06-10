package database;

public class TableInexistanteException extends Throwable {
    public TableInexistanteException() {
        super("Table inexistante");
    }
}
