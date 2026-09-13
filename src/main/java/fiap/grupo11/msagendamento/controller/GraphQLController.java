package fiap.grupo11.msagendamento.controller;

import fiap.grupo11.msagendamento.dto.AgendamentoGraphQLResponse;
import fiap.grupo11.msagendamento.service.AgendamentoService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1/graphql")
public class GraphQLController {

    private final AgendamentoService service;

    public GraphQLController(AgendamentoService service) {
        this.service = service;
    }

    @QueryMapping
    public List<AgendamentoGraphQLResponse> historicoPaciente(@Argument Long patientId, @Argument Boolean futureOnly, Authentication authentication) {
        return service.historicoPaciente(patientId, Boolean.TRUE.equals(futureOnly), authentication);
    }

}
