package fiap.grupo11.msagendamento.exception;

public class AgendamentoNotFoundException extends RuntimeException {

    public AgendamentoNotFoundException(Long id) {
        super("Appointment not found: " + id);
    }
}