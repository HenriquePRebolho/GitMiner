package aiss.gitminer.controller;

import aiss.gitminer.exception.ProjectNotFoundException;
import aiss.gitminer.model.*;
import aiss.gitminer.repository.CommitRepository;
import aiss.gitminer.repository.IssueRepository;
import aiss.gitminer.repository.ProjectRepository;
import aiss.gitminer.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;

@Tag(name = "Project", description = "Project management API")
@RestController // indicar que es controlador
@RequestMapping("/gitminer/projects")
public class ProjectController {

    @Autowired // cargar repositorio de projects con datos
    ProjectRepository projectRepository;

    @Autowired
    CommitRepository commitRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    IssueRepository issueRepository;

    // GET http://localhost:8080/giminer/projects
    @Operation(
            summary = "Get a list of all projects",
            description = "Retrieve a list of all projects",
            tags = { "projects", "get" })
    @ApiResponses({
                @ApiResponse(responseCode = "200", content =
                        {@Content(schema = @Schema(implementation = Project.class),
                                mediaType = "application/json")})
    })
    @GetMapping // especificar metodo HTTP a utilizar
    public List<Project> findAll (@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String order,
                                  @RequestParam(defaultValue = "0") int page,
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

        Page<Project> pageProjects;

        if (name == null) {
            pageProjects = projectRepository.findAll(paging);
        }
        else {
            pageProjects = projectRepository.findByName(name, paging);
        }
        return pageProjects.getContent();
    }


    // GET http://localhost:8080/giminer/projects/:projectId
    @Operation(
            summary = "Get a project by id",
            description = "Find a project by it's id",
            tags = {"get by id", "project"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = Project.class),
                            mediaType = "application/json")})
    })
    @GetMapping("/{id}")
    public Project findById(@Parameter(description = "id of a project to be searched")
                           @PathVariable Long id) throws ProjectNotFoundException {
        Optional<Project> foundProject = projectRepository.findById(id);

        if (!foundProject.isPresent()) {
            throw new ProjectNotFoundException();
        }
        return foundProject.get();
    }


    // POST http://localhost:8080/gitminer/projects
    @Operation(
            summary = "Post a new project",
            description = "Create a new project",
            tags = {"post", "project"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(implementation = Project.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Project createProject(@RequestBody Project project) {
        try {
            if (projectRepository.findByName(project.getName()) == null) {

                if (project.getIssues() != null) {

                    for (Issue i : project.getIssues()) {

                        if (i.getAuthor() != null) {
                            Optional<User> existingAuthor = userRepository.findByWebUrl(i.getAuthor().getWebUrl());
                            if (existingAuthor.isEmpty()) {
                                userRepository.save(i.getAuthor());
                            }
                        }

                        if (i.getAssignee() != null) {
                            Optional<User> existingAssignee = userRepository.findByWebUrl(i.getAssignee().getWebUrl());
                            if (existingAssignee.isEmpty()) {
                                userRepository.save(i.getAssignee());
                            }
                        }

                        if (i.getComments() != null) {
                            for (Comment c : i.getComments()) {
                                if (c.getAuthor() != null) {
                                    Optional<User> existingCommentAuthor = userRepository.findByWebUrl(c.getAuthor().getWebUrl());
                                    if (existingCommentAuthor.isEmpty()) {
                                        userRepository.save(c.getAuthor());
                                    }
                                }
                            }
                        }
                    }
                }

                return projectRepository.save(project);
            }

            return projectRepository.findByName(project.getName());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    // DELETE http://localhost:8080/api/projects/:projectId
    @Operation(
            summary = "Delete a new project",
            description = "Erase a new project",
            tags = {"delete", "project"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema(implementation = Project.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteProjectById(@Parameter (
            description = "id of the project to be delete") @PathVariable Long id)
            throws ProjectNotFoundException{
        Optional<Project> foundProject = projectRepository.findById(id);

        if (!foundProject.isPresent()) {
            throw new ProjectNotFoundException();
        }
        projectRepository.deleteById(id);
    }


}
