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

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public AuthorDto insert(AuthorDto dto) {
        Author entity = modelMapper.map(dto, Author.class);
        entity = repository.save(entity);
        return modelMapper.map(entity, AuthorDto.class);
    }

    @Transactional
    public AuthorDto update(Long id, AuthorDto dto) {
        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id %d not found".formatted(id)));
        Author entity = modelMapper.map(dto, Author.class);
        try {
            repository.save(entity);
            return modelMapper.map(entity, AuthorDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation");
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
    }
}
