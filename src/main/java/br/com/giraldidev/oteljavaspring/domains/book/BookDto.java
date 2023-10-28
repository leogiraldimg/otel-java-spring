package br.com.giraldidev.oteljavaspring.domains.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.giraldidev.oteljavaspring.domains.author.Author;
import br.com.giraldidev.oteljavaspring.domains.author.AuthorDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    private Long id;
    private String name;

    private List<AuthorDto> authors = new ArrayList<>();

    public BookDto(Book entity) {
        id = entity.getId();
        name = entity.getName();
    }

    public BookDto(Book entity, Set<Author> authors) {
        this(entity);
        authors.forEach(author -> this.authors.add(new AuthorDto(author)));
    }
}
