package fiap.grupo11.msagendamento.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Test
    void migrationSeedsOneUserForEachApplicationRole() {
        assertThat(repository.findAll())
                .extracting(usuario -> usuario.getUsername() + ":" + usuario.getRole())
                .containsExactlyInAnyOrder(
                        "medico:MEDICO",
                        "enfermeiro:ENFERMEIRO",
                        "paciente:PACIENTE");
    }

    @Test
    void seededPasswordsAreStoredAsBcryptHashes() {
        assertThat(repository.findByUsername("medico"))
                .get()
                .extracting("passwordHash")
                .asString()
                .startsWith("$2a$10$");
    }
}