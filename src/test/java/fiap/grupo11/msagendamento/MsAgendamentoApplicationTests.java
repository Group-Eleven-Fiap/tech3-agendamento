package fiap.grupo11.msagendamento;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MsAgendamentoApplicationTests {

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    @Test
    void contextLoads() {
    }

    @Test
    void usesTheComposeRabbitCredentialsByDefault() {
        assertThat(rabbitUsername).isEqualTo("agendamento");
        assertThat(rabbitPassword).isEqualTo("agendamento");
    }

}
