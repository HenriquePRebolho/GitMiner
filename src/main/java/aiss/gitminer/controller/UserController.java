package aiss.gitminer.controller;

import aiss.gitminer.exception.ProjectNotFoundException;
import aiss.gitminer.exception.UserNotFoundException;
import aiss.gitminer.model.Project;
import aiss.gitminer.model.User;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "User", description = "User management API")
@RestController // indicar que es controlador
@RequestMapping("/gitminer/users")
public class UserController {

    @Autowired // cargar repositorio de user con datos
    UserRepository userRepository;

    // Devolver todos los users
    @Operation(
            summary = "Retrieve a list of all users",
            description = "Get a list of all users",
            tags = { "users", "get" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = User.class),
                            mediaType = "application/json")})
    })
    @GetMapping // especificar metodo HTTP a utilizar
    public List<User> findAll (@RequestParam(required = false) String name,
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

        Page<User> pageUsers;

        if (name == null) {
            pageUsers = userRepository.findAll(paging);
        }
        else {
            pageUsers = userRepository.findByName(name, paging);
        }
        return pageUsers.getContent();
    }


    // GET https://localhost:8080/gitminer/users/:userId
    @Operation(
            summary = "Get a user by id",
            description = "Find a user by it's id",
            tags = {"get by id", "user"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content =
                    {@Content(schema = @Schema(implementation = User.class),
                            mediaType = "application/json")})
    })
    @GetMapping("/{id}")
    public User findById(@Parameter(description = "id of an user to be searched")
                            @PathVariable Long id) throws UserNotFoundException {
        Optional<User> foundUser = userRepository.findById(id);

        if (!foundUser.isPresent()) {
            throw new UserNotFoundException();
        }
        return foundUser.get();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }


}
