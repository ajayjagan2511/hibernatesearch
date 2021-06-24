package org.acme;


import io.quarkus.runtime.StartupEvent;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jboss.resteasy.annotations.jaxrs.FormParam;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Path("/library")
public class LibraryResource {

    @PUT
    @Path("book")
    @Transactional
    public void addBook(@FormParam String title, @FormParam Long authorId) {
        Author author = Author.findById(authorId);
        if (author != null) {
            Book book = new Book();
            book.title = title;
            book.author = author;
            book.persist();

            author.books.add(book);
            author.persist();
        }
    }


    @PUT
    @Path("author")
    @Transactional
    public void addAuthor(@FormParam String firstName, @FormParam String lastName) {
        Author author = new Author();
        author.firstName = firstName;
        author.lastName = lastName;
        author.persist();
    }

    @POST
    @Path("author/{id}")
    @Transactional
    public void updateAuthor(@PathParam Long id, @FormParam String firstName, @FormParam String lastName) {
        Author author = Author.findById(id);
        if (author != null) {
            author.firstName = firstName;
            author.lastName = lastName;
            author.persist();
        }
    }
    @DELETE
    @Path("book/{id}")
    @Transactional
    public void deleteBook(@PathParam Long id) {
        Book book = Book.findById(id);
        if (book != null) {
            book.author.books.remove(book);
            book.delete();
        }
    }


    @DELETE
    @Path("author/{id}")
    @Transactional
    public void deleteAuthor(@PathParam Long id) {
        Author author = Author.findById(id);
        if (author != null) {
            author.delete();
        }
    }

    @Inject
    SearchSession searchSession;

    @Transactional
    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        // only reindex if we imported some content
        if (Book.count() > 0) {
            searchSession.massIndexer()
                    .startAndWait();
        }
    }

    @GET
    @Path("author/search")
    @Transactional
    public List<Author> searchAuthors(@org.jboss.resteasy.annotations.jaxrs.QueryParam String pattern,
                                      @QueryParam Optional<Integer> size) {
        return searchSession.search(Author.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .fields("firstName", "lastName", "books.title").matching(pattern)
                )
                .sort(f -> f.field("lastName_sort").then().field("firstName_sort"))
                .fetchHits(size.orElse(20));
    }
    @GET
    @Path("author/time/search")
    @Transactional
    public Duration searchAuthorsTime(@QueryParam String pattern,
                                      @QueryParam Optional<Integer> size) {
        SearchResult<Author> result= searchSession.search(Author.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .fields("firstName", "lastName","books.title").matching(pattern)
                )
                .sort(f -> f.field("lastName_sort").then().field("firstName_sort"))
                .fetchAll();
        return result.took();
    }
    @GET
    @Path("author/time/new/search")
    @Transactional
    public Duration searchAuthorsTime_new(@QueryParam String pattern,
                                      @QueryParam Optional<Integer> size) {
        SearchResult<Author> result= searchSession.search(Author.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .fields("firstName", "lastName").matching(pattern).analyzer("name_new")
                )
                .sort(f -> f.field("lastName_sort").then().field("firstName_sort"))
                .fetchAll();
        return result.took();
    }

    @GET
    @Path("book/search")
    @Transactional
    public List<Book> searchBook(@org.jboss.resteasy.annotations.jaxrs.QueryParam String pattern,
                                      @QueryParam Optional<Integer> size) {
        return searchSession.search(Book.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .fields("title").matching(pattern)
                )
                .fetchAllHits();
    }
    @GET
    @Path("book/wildcard/search")
    @Transactional
    public List<Book> searchBookWildcard(@org.jboss.resteasy.annotations.jaxrs.QueryParam String pattern,
                                 @QueryParam Optional<Integer> size) {
        return searchSession.search(Book.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.wildcard()
                                        .fields("title").matching(pattern)
                )
                .fetchAllHits();
    }
    @GET
    @Path("book/time/search")
    @Transactional
    public Duration searchBookTime(@QueryParam String pattern,
                                @QueryParam Optional<Integer> size) {
        SearchResult<Book> result= searchSession.search(Book.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .fields("title").matching(pattern)
                )
                .fetchAll();
        return result.took();
    }
    @GET
    @Path("book/wildcard/time/search")
    @Transactional
    public Duration searchBookWildcardTime(@QueryParam String pattern,
                                   @QueryParam Optional<Integer> size) {
        SearchResult<Book> result= searchSession.search(Book.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.wildcard()
                                        .fields("title").matching(pattern)
                )
                .fetchAll();
        return result.took();
    }
    @GET
    @Path("book/phrase/search")
    @Transactional
    public List<Book> searchPhrasematch(@org.jboss.resteasy.annotations.jaxrs.QueryParam String pattern,
                                 @QueryParam Optional<Integer> size) {
        return searchSession.search(Book.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.phrase()
                                        .fields("title").matching(pattern)
                )
                .fetchAllHits();
    }
    @GET
    @Path("book/phrase/time/search")
    @Transactional
    public Duration searchBookPhraseTime(@QueryParam String pattern,
                                           @QueryParam Optional<Integer> size) {
        SearchResult<Book> result= searchSession.search(Book.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.phrase()
                                        .fields("title").matching(pattern)
                )
                .fetchAll();
        return result.took();
    }



}
