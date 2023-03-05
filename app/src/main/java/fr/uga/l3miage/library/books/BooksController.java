package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper) {
       this.bookService = bookService;
        this.booksMapper = booksMapper;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/books?q=peripheral")
    public Collection<BookDTO> getFilteredBooks(@RequestParam("q") String query) {
      Collection<Book> books;
      if (query == null) {
        books = bookService.list();
      } else {
        books = bookService.findByTitle(query);
      }
      return books.stream()
              .map(booksMapper::entityToDTO)
              .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/books")
    public Collection<BookDTO> getBooks() {
      Collection<Book> books;
      books = bookService.list();
      return books.stream()
              .map(booksMapper::entityToDTO)
              .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/authors/{authorId}/books")
    public Collection<BookDTO> findAuthorBooks(@PathVariable("authorId") Long authorId) {
      Collection<Book> books;
      try {
        books = this.bookService.getByAuthor(authorId);
      } catch (EntityNotFoundException e) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      return books.stream()
              .map(booksMapper::entityToDTO)
              .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/books/{id}")
    public BookDTO book(@PathVariable("id") Long id) {
      Book book;

      try {
        book = this.bookService.get(id);
      } catch (EntityNotFoundException e) {
        System.err.println("The book was not found");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      
      return this.booksMapper.entityToDTO(book);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/authors/{id}/books")
    public BookDTO newBook(@PathVariable("id") Long authorId, @RequestBody BookDTO bookDTO) {    

        if (bookDTO.language() != null 
            && !bookDTO.language().toUpperCase().equals("FRENCH")
            && !bookDTO.language().toUpperCase().equals("ENGLISH")) {
          System.err.println("Wrong language");
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (String.valueOf(bookDTO.isbn()).length() < 10 || String.valueOf(bookDTO.isbn()).length() > 13) {
          System.err.println("Wrong isbn");
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (bookDTO.year() < -9999 || bookDTO.year() > 9999) {
          System.err.println("Wrong year");
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
      
        Book book = this.booksMapper.dtoToEntity(bookDTO);

        if (book.getTitle() == null) {
            System.err.println("Title is required");

          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } 
        try {
            book = this.bookService.save(authorId, book);
        } catch (EntityNotFoundException e) {
            System.err.println("The author was not found");

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return this.booksMapper.entityToDTO(book);
    }

    
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/books/{bookId}") 
    public BookDTO updateBook(@RequestBody BookDTO bookDTO,@PathVariable("bookId") Long idBook) {
        if (bookDTO.id() != idBook) {
          System.err.println("The book could not be validated");
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }

      Book book = null;

      try {
          book = this.bookService.get(idBook);
      }catch(EntityNotFoundException e) {
          System.err.println("The book was not found");

          throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }

      Book updatedBook = null;

      try {
          updatedBook = this.bookService.update(this.booksMapper.dtoToEntity(bookDTO));
      } catch(EntityNotFoundException e) {
          System.err.println("Error: the book could not be updated");

          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
      }
      return this.booksMapper.entityToDTO(updatedBook);
    }


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/books/{newBookId}")
    public void deleteBook(@PathVariable("newBookId") Long id) {
    Book book = null;
    try {
      book = this.bookService.get(id);
    } catch(EntityNotFoundException e) {
      System.err.println("The book was not found");
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    try {
     this.bookService.delete(id);
    } catch (EntityNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }


    public void addAuthor(Long authorId, AuthorDTO author) {

    }
}
