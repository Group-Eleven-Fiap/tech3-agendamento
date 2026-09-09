package fiap.grupo11.msagendamento.exception;

public class AgendamentoConflictException extends RuntimeException {

    public AgendamentoConflictException() {
        super("The professional already has an appointment in the requested period");
    }
}