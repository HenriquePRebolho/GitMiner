package aiss.gitminer.controller;

import aiss.gitminer.exception.CommentNotFoundException;
import aiss.gitminer.model.Comment;
import aiss.gitminer.model.Project;
import aiss.gitminer.repository.CommentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Tag(name = "Comment", description = "Comment management API")
@RestController // indicar que es controlador
@RequestMapping("/gitminer/comments")
public class CommentController {

    @Autowired // cargar repositorio de comment con datos
    CommentRepository commentRepository;

    // Devolver todos los comments
    @Operation(
            summary = "Retrieve a list of all comments",
            description = "Get a list of all comments",
            tags = { "comments", "get" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = Comment.class),
                            mediaType = "application/json")})
    })
    @GetMapping // especificar metodo HTTP a utilizar
    public List<Comment> findAll (@RequestParam(required = false) String name,
                               @RequestParam(required = false) String order,
                               @RequestParam(defaultValue = "5") int page,
                               @RequestParam(defaultValue = "5") int size) {
        Pageable paging;

        if (order != null) {
            if (order.startsWith("-")) {
                paging = PageRequest.of(page, size, Sort.by(order.substring(1)).descending());
            }
            else {
                paging = PageRequest.of(page, size, Sort.by(order).ascending());
            }
        }
        else {
            paging = PageRequest.of(page, size);
        }

        Page<Comment> pageComments;

        if (name == null) {
            pageComments = commentRepository.findAll(paging);
        }
        else {
            pageComments = commentRepository.findByAuthor(name, paging);
        }
        return pageComments.getContent();
    }

    // GET http:localhost:8080/gitminer/comments/:id
    @Operation(
            summary = "Get a comment by id",
            description = "Find a comment by it's id",
            tags = {"get by id", "comment"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = Comment.class),
                            mediaType = "application/json")})
    })
    @GetMapping("/{id}")
    public Comment findById(@Parameter(description = "id of a comment to be searched")
                            @PathVariable Long id) throws CommentNotFoundException {
        Optional<Comment> foundComment = commentRepository.findById(id);

        if (!foundComment.isPresent()) {
            throw new CommentNotFoundException();
        }
        return foundComment.get();
    }


    // POST http://localhost:8080/gitminer/comment
    @Operation(
            summary = "Post a new comment",
            description = "Create a new comment",
            tags = {"post", "comment"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(implementation = Comment.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping()
    public Comment createComment(@Valid @RequestBody Comment comment) {
        Comment newComment = commentRepository.save(
                new Comment(comment.getBody(), comment.getAuthor(),
                                comment.getCreatedAt(), comment.getUpdatedAt()));
        return newComment;
    }
}
