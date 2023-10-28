package br.com.giraldidev.oteljavaspring.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.giraldidev.oteljavaspring.domains.book.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
