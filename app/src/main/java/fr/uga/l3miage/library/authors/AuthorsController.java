package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.DeleteAuthorException;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import jakarta.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;

import java.util.Collection;
import java.util.Collections;

@RestController  
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/authors")
    public Collection<AuthorDTO> authors(@RequestParam(value = "q", required = false) String query) {
        Collection<Author> authors;
        if (query == null) {
            authors = authorService.list();
        } else {
            authors = authorService.searchByName(query);
        }
        return authors.stream()
                .map(authorMapper::entityToDTO)
                .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/authors/{id}")
    public AuthorDTO author(@PathVariable("id") Long id) {

        Author author = null;

        try {
            author = this.authorService.get(id);

        } catch(EntityNotFoundException e){
            //pas d'auteur correspond à l'id donné 

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return this.authorMapper.entityToDTO(author);
    }

    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDTO newAuthor(@RequestBody AuthorDTO authorDTO)  {
                
        Author author = this.authorMapper.dtoToEntity(authorDTO);
       
        if (authorDTO.fullName().isBlank()) {

            System.err.println("The author could not be validated");

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        author = this.authorService.save(author); // => création de l'ID

        return this.authorMapper.entityToDTO(author);
    }
    
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/authors/{id}")
    public AuthorDTO updateAuthor(@RequestBody AuthorDTO authorDTO,@PathVariable("id") Long id) {
        
        if (authorDTO.id() != id) {
            System.err.println("The author could not be validated");

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Author author = null;

        try {
            author = this.authorService.get(id);
        }catch(EntityNotFoundException e) {
            System.err.println("The author was not found");

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Author updatedAuthor = null;

        try {
            updatedAuthor = this.authorService.update(this.authorMapper.dtoToEntity(authorDTO));
        } catch(EntityNotFoundException e) {
            System.err.println("Error: the author could not be updated");

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return this.authorMapper.entityToDTO(updatedAuthor);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("authors/{id}")
    public void deleteAuthor(@PathVariable Long id) {
         Author author = null;
         try {
            author = this.authorService.get(id);
         } catch(EntityNotFoundException e) {
            System.err.println("The author was not found");
            
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
         }
         try {
            this.authorService.delete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (DeleteAuthorException e) {
            System.err.println("this author share authority on a book, then book should be removed first");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }    
    }

    public Collection<BookDTO> books(Long authorId) {
        return Collections.emptyList();
    }

}
