package aiss.gitminer.controller;

import aiss.gitminer.exception.IssueNotFoundException;
import aiss.gitminer.model.Comment;
import aiss.gitminer.model.Issue;
import aiss.gitminer.repository.IssueRepository;
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

@Tag(name = "Issue", description = "Issue management API")
@RestController // indicar que es controlador
@RequestMapping("/gitminer/issues")
public class IssueControler {

    @Autowired // cargar repositorio de issue con datos
    IssueRepository issueRepository;

    // Devolver todos los projects
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

        Page<Issue> pageIssues;

        if (state == null) {
            pageIssues = issueRepository.findAll(paging);
        }
        else {
            pageIssues = issueRepository.findByState(state, paging);
        }
        return pageIssues.getContent();
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

    @Operation(
            summary = "Create a new issue",
            description = "Create and return a new issue",
            tags = { "issue", "post" }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {
                    @Content(schema = @Schema(implementation = Issue.class),
                            mediaType = "application/json") })
    })
    @PostMapping
    public Issue createIssue(@RequestBody Issue issue) {
        return issueRepository.save(issue);
    }
    // GET http://localhost:8080/gitminer/issues/:issueId/comments
    @Operation(
            summary = "Retrieve a list of all comments from a specified issue",
            description = "Get a list of all comments from a specified issue",
            tags = { "comments", "get", "issue" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = Issue.class),
                            mediaType = "application/json")})
    })
    @GetMapping("/:id/comments") // especificar metodo HTTP a utilizar
    public List<Comment> findIssueComments (
                                @Parameter(description = "id of the issue to be searched")
                                              @PathVariable Long id,
                                @RequestParam(required = false) String order,
                                @RequestParam(defaultValue = "5") int page,
                                @RequestParam(defaultValue = "5") int size)
            throws IssueNotFoundException {

        Optional<Issue> issueFound = issueRepository.findById(id);

        if (!issueFound.isPresent()) {
            throw new IssueNotFoundException();
        }
        List<Comment> issueComments = issueFound.get().getComments();

        // ""Orderding"" if set to reverse order
        if (order != null) {
            if (order.startsWith("-")) {
                issueComments.sort(Collections.reverseOrder());
            }
        }
        // ""Pagination""
        int numComments = issueComments.size();
        int numPages = numComments / page;

        return issueComments.subList(numPages, numPages+size); // using "pagination"
    }



}
