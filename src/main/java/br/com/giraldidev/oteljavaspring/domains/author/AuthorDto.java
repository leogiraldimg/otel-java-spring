package br.com.giraldidev.oteljavaspring.domains.author;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDto {

    private Long id;
    private String name;

    public AuthorDto(Author entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
