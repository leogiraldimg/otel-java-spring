package br.com.giraldidev.oteljavaspring.controllers;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.giraldidev.oteljavaspring.domains.author.AuthorDto;
import br.com.giraldidev.oteljavaspring.services.AuthorService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.SemanticAttributes;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/authors")
public class AuthorController {

    @Autowired
    private AuthorService service;

    private final Tracer tracer;
    private final Meter meter;

    private static final AttributeKey<String> AUTHOR_KEY = stringKey("author");

    private String className = AuthorController.class.getName();

    @Autowired
    public AuthorController(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(className);
        meter = openTelemetry.getMeter(className);
    }

    @PostMapping
    public ResponseEntity<AuthorDto> insert(@RequestBody AuthorDto dto) {
        dto = service.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<AuthorDto> update(@PathVariable Long id, @RequestBody AuthorDto dto,
            HttpServletRequest request) {
        String codeFunction = "%s.%s".formatted(className, "update");
        LongCounter counter = meter.counterBuilder("author_updated_count").setUnit("unit").build();
        Span span = tracer.spanBuilder(codeFunction).setSpanKind(SpanKind.SERVER)
                .setAttribute(SemanticAttributes.CODE_FUNCTION, codeFunction)
                .setAttribute(SemanticAttributes.HTTP_REQUEST_METHOD, "PUT")
                .setAttribute(SemanticAttributes.SERVER_ADDRESS, request.getLocalAddr())
                .setAttribute(SemanticAttributes.SERVER_PORT, (long) request.getLocalPort())
                .setAttribute(SemanticAttributes.URL_FULL, request.getRequestURL().toString())
                .setAttribute(SemanticAttributes.URL_PATH, "/authors/%d".formatted(id))
                .setAttribute(SemanticAttributes.URL_SCHEME, request.getScheme()).startSpan();

        try (Scope scope = span.makeCurrent()) {
            dto = service.update(id, dto);
            Attributes attributes = Attributes.of(AUTHOR_KEY, dto.getId().toString());
            counter.add(1L, attributes, Context.current());
            return ResponseEntity.ok().body(dto);
        } finally {
            span.end();
        }
    }

    @GetMapping
    public ResponseEntity<Page<AuthorDto>> findAllPaged(Pageable pageable) {
        Page<AuthorDto> page = service.findAllPaged(pageable);
        return ResponseEntity.ok().body(page);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<AuthorDto> findById(@PathVariable Long id) {
        AuthorDto dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
