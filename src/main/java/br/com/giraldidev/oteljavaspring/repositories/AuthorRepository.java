package br.com.giraldidev.oteljavaspring.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.giraldidev.oteljavaspring.domains.author.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

}
