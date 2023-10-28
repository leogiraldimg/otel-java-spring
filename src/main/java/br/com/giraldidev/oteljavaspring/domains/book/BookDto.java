package br.com.giraldidev.oteljavaspring.domains.book;

import java.util.ArrayList;
import java.util.List;

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
}
