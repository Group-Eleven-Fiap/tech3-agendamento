package fiap.grupo11.msagendamento.controller;

import fiap.grupo11.msagendamento.dto.AgendamentoRequest;
import fiap.grupo11.msagendamento.dto.AgendamentoResponse;
import fiap.grupo11.msagendamento.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse create(@Valid @RequestBody AgendamentoRequest request,
                                      Authentication authentication) {
        return service.create(request, authentication);
    }

    @GetMapping
    public List<AgendamentoResponse> list(Authentication authentication) {
        return service.list(authentication);
    }

    @GetMapping("/me")
    public List<AgendamentoResponse> listMine(Authentication authentication) {
        return service.listMine(authentication);
    }

    @GetMapping("/{id}")
    public AgendamentoResponse get(@PathVariable Long id, Authentication authentication) {
        return service.get(id, authentication);
    }

    @PutMapping("/{id}")
    public AgendamentoResponse update(@PathVariable Long id,
                                      @Valid @RequestBody AgendamentoRequest request,
                                      Authentication authentication) {
        return service.update(id, request, authentication);
    }
}