package aiss.gitminer.controller;

import aiss.gitminer.exception.IssueNotFoundException;
import aiss.gitminer.exception.UserNotFoundException;
import aiss.gitminer.model.Comment;
import aiss.gitminer.model.Issue;
import aiss.gitminer.model.User;
import aiss.gitminer.repository.IssueRepository;
import aiss.gitminer.repository.UserRepository;
import com.sun.tools.jconsole.JConsoleContext;
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
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Issue", description = "Issue management API")
@RestController // indicar que es controlador
@RequestMapping("/gitminer/issues")
public class IssueControler {

    @Autowired // cargar repositorio de issue con datos
    IssueRepository issueRepository;

    @Autowired
    UserRepository userRepository;

    // GET http://localhost:8080/gitminer/issues
    @Operation(
            summary = "Retrieve a list of all issues",
            description = "Get a list of all issues",
            tags = { "projects", "get" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = Issue.class),
                            mediaType = "application/json")})
    })
    @GetMapping // especificar metodo HTTP a utilizar
    public List<Issue> findAll (@RequestParam(required = false) String state,
                                @RequestParam(required = false) Long authorId,
                                  @RequestParam(required = false) String order,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size)
            throws UserNotFoundException{
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

        Page<Issue> pageIssues;

        if (state != null && authorId == null){
            pageIssues = issueRepository.findByState(state, paging);
            return pageIssues.getContent();
        }
        else if (state == null && authorId != null) {
            Optional<User> userFound = userRepository.findById(authorId);

            if (!userFound.isPresent()) {
                throw new UserNotFoundException();
            }

            List<Issue> issuesByAuthorId = issueRepository.findAll();
            issuesByAuthorId = issuesByAuthorId.stream()
                    .filter(issue -> issue.getAuthor().getId().equals(authorId))
                    .collect(Collectors.toList());

            return issuesByAuthorId;
        }
        else {
            pageIssues = issueRepository.findAll(paging);
            return pageIssues.getContent();
        }
    }


    // GET http://localhost:8080/gitminer/issues/:issueId
    @Operation(
            summary = "Get an issue by id",
            description = "Find an issue by it's id",
            tags = {"get by id", "issue"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = Issue.class),
                            mediaType = "application/json")})
    })
    @GetMapping("/{id}")
    public Issue findById(@Parameter(description = "id of an issue to be searched")
                            @PathVariable Long id) throws IssueNotFoundException {
        Optional<Issue> foundIssue = issueRepository.findById(id);

        if (!foundIssue.isPresent()) {
            throw new IssueNotFoundException();
        }
        return foundIssue.get();
    }

    
    // GET http://localhost:8080/gitminer/issues/:issueId/comments
    @Operation(
            summary = "Retrieve a list of all comments from a specified issue",
            description = "Get a list of all comments from a specified issue",
            tags = { "comments", "get", "issue" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = Comment.class),
                            mediaType = "application/json")})
    })
    @GetMapping("/{id}/comments") // especificar metodo HTTP a utilizar
    public List<Comment> findIssueComments (
                                @Parameter(description = "id of the issue to be searched")
                                              @PathVariable Long id) throws IssueNotFoundException {
        Optional<Issue> issueFound = issueRepository.findById(id);

        if (!issueFound.isPresent()) {
            throw new IssueNotFoundException();
        }
        List<Comment> issueComments = issueFound.get().getComments();

        return issueComments;
    }

}
