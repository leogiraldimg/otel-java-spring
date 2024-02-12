package br.com.giraldidev.oteljavaspring.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.giraldidev.oteljavaspring.domains.author.Author;
import br.com.giraldidev.oteljavaspring.domains.author.AuthorDto;
import br.com.giraldidev.oteljavaspring.repositories.AuthorRepository;
import br.com.giraldidev.oteljavaspring.services.exception.DatabaseException;
import br.com.giraldidev.oteljavaspring.services.exception.ResourceNotFoundException;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.SemanticAttributes;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthorService {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    private final Tracer tracer;

    private String className = AuthorService.class.getName();

    @Autowired
    public AuthorService(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(className);
    }

    @Transactional
    public AuthorDto insert(AuthorDto dto) {
        Author entity = modelMapper.map(dto, Author.class);
        entity = repository.save(entity);

        log.info("Author with id %d created".formatted(entity.getId()));

        return modelMapper.map(entity, AuthorDto.class);
    }

    @Transactional
    public AuthorDto update(Long id, AuthorDto dto) {
        String codeFunction = "%s.%s".formatted(className, "update");
        Span span = tracer.spanBuilder(codeFunction).setSpanKind(SpanKind.INTERNAL)
                .setAttribute(SemanticAttributes.CODE_FUNCTION, codeFunction).startSpan();

        try (Scope scope = span.makeCurrent()) {
            Author entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Author with id %d not found".formatted(id)));
            entity.setName(dto.getName());

            repository.save(entity);

            log.info("Author with id %d updated".formatted(id));
            span.addEvent("Author with id %d updated".formatted(id));

            return modelMapper.map(entity, AuthorDto.class);
        } catch (DataIntegrityViolationException e) {
            span.setStatus(StatusCode.ERROR, "DataIntegrityViolationException: %s".formatted(e.getMessage()));
            span.recordException(e);

            throw new DatabaseException("Integrity violation");
        } catch (ResourceNotFoundException e) {
            span.setStatus(StatusCode.ERROR, "ResourceNotFoundException: %s".formatted(e.getMessage()));
            span.recordException(e);

            throw new ResourceNotFoundException(e.getMessage());
        } finally {
            span.end();
        }
    }

    @Transactional(readOnly = true)
    public Page<AuthorDto> findAllPaged(Pageable pageable) {
        Page<Author> page = repository.findAll(pageable);
        return page.map(author -> modelMapper.map(author, AuthorDto.class));
    }

    @Transactional(readOnly = true)
    public AuthorDto findById(Long id) {
        Author entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id %d not found".formatted(id)));
        return modelMapper.map(entity, AuthorDto.class);
    }

    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id %d not found".formatted(id)));
        repository.deleteById(id);

        log.info("Author with id %d deleted".formatted(id));
    }
}
