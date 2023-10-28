CREATE TABLE tb_book_author (
    author_id INT NOT NULL,
    book_id INT NOT NULL,
    FOREIGN KEY (author_id) REFERENCES tb_author(id),
    FOREIGN KEY (book_id) REFERENCES tb_book(id)
);