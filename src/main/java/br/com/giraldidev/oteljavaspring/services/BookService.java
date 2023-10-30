package br.com.giraldidev.oteljavaspring.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.giraldidev.oteljavaspring.domains.author.Author;
import br.com.giraldidev.oteljavaspring.domains.author.AuthorDto;
import br.com.giraldidev.oteljavaspring.domains.book.Book;
import br.com.giraldidev.oteljavaspring.domains.book.BookDto;
import br.com.giraldidev.oteljavaspring.repositories.AuthorRepository;
import br.com.giraldidev.oteljavaspring.repositories.BookRepository;
import br.com.giraldidev.oteljavaspring.services.exception.DatabaseException;
import br.com.giraldidev.oteljavaspring.services.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookService {

    @Autowired
    private BookRepository repository;

    @Autowired
    private AuthorRepository authorRepository;

    @Transactional
    public BookDto insert(BookDto dto) {
        Book entity = new Book();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);

        log.info("Book with id %d created".formatted(entity.getId()));

        return new BookDto(entity, entity.getAuthors());
    }

    @Transactional
    public BookDto update(Long id, BookDto dto) {
        Book entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id %d not found".formatted(id)));
        copyDtoToEntity(dto, entity);
        try {
            entity = repository.save(entity);

            log.info("Book with id %d updated".formatted(id));

            return new BookDto(entity, entity.getAuthors());
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation");
        }
    }

    @Transactional(readOnly = true)
    public Page<BookDto> findAllPaged(Pageable pageable) {
        Page<Book> page = repository.findAll(pageable);
        return page.map(book -> new BookDto(book, book.getAuthors()));
    }

    @Transactional(readOnly = true)
    public BookDto findById(Long id) {
        Book entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id %d not found".formatted(id)));
        return new BookDto(entity, entity.getAuthors());
    }

    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id %d not found".formatted(id)));
        repository.deleteById(id);

        log.info("Book with id %d deleted".formatted(id));
    }

    private void copyDtoToEntity(BookDto dto, Book entity) {
        entity.setId(dto.getId());
        entity.setName(dto.getName());

        entity.getAuthors().clear();
        for (AuthorDto authorDto : dto.getAuthors()) {
            Long authorId = authorDto.getId();
            Author author = authorRepository.findById(authorId).orElseThrow(
                    () -> new ResourceNotFoundException("Author with id %d not found".formatted(authorId)));
            entity.getAuthors().add(author);
        }
    }
}
