package activeRecord;

public class TableInexistanteException extends Throwable {
    public TableInexistanteException() {
        super("Table inexistante");
    }
}
