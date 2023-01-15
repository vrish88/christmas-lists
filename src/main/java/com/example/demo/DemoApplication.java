package com.example.demo;

import jakarta.persistence.*;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .build();
    }
    @Bean
    public ApplicationRunner runner(PersonRepo repo) {
        return (args -> {
            PersonEntity steven = new PersonEntity();
            steven.setName("Steven");
            steven.setBuyingFor("Jo");
            repo.save(steven);

            PersonEntity jo = new PersonEntity();
            jo.setName("Jo");
            jo.setBuyingFor("Steven");
            repo.save(jo);
        });
    }

    @Controller
    static class PersonController {
        private final PersonRepo repo;

        PersonController(PersonRepo repo) {
            this.repo = repo;
        }

        @GetMapping("/")
        public ModelAndView index() {
            ModelAndView mv = new ModelAndView("index");
            setupPersonForm(mv, new PersonForm("", ""));
            return mv;
        }

        private void setupPersonForm(ModelAndView mv, PersonForm personForm) {
            mv.addObject("persons", repo.findAll());
            mv.addObject("personForm", personForm);
        }

        @PostMapping("/person/{personId}/items")
        public String createItemInList(@PathVariable("personId") Long personId, ItemEntity itemEntity) {
            PersonEntity personEntity = repo.findById(personId).get();
            ArrayList<ItemEntity> items = new ArrayList<>(personEntity.getItems());
            items.add(itemEntity);
            personEntity.setItems(items);
            repo.save(personEntity);

            return "redirect:/person/" + personEntity.getId();
        }

        @DeleteMapping("/person/{personId}/items/{itemId}")
        public ModelAndView removeListItem(@PathVariable("personId") Long personId, @PathVariable("itemId") Long itemId) {
            PersonEntity personEntity = repo.findById(personId).get();
            personEntity.getItems().removeIf(i -> i.getId().equals(itemId));
            repo.save(personEntity);

            ModelAndView mv = new ModelAndView("person-show");
            mv.addObject("person", personEntity);
            return mv;
        }

        @PostMapping("/person")
        public ModelAndView createPerson(@Valid PersonForm personForm, BindingResult bindingResult, HttpServletResponse response) {
            if (bindingResult.hasErrors()) {
                ModelAndView view = new ModelAndView("person-form");
                view.setStatus(HttpStatusCode.valueOf(422));
                setupPersonForm(view, personForm);
                return view;
            }
            PersonEntity personEntity = new PersonEntity();
            personEntity.setName(personForm.getName());
            personEntity.setBuyingFor(personForm.getBuyingFor());

            repo.save(personEntity);
            response.setHeader("HX-Location", "/person/" + personEntity.getId());
            response.setStatus(204);
            return null;
        }

        @GetMapping("/person/{id}")
        public String getPerson(@PathVariable("id") Long id, Model model) {
            Optional<PersonEntity> byId = repo.findById(id);

            model.addAttribute("person", byId.get());
            return "person-show";
        }

        @Value
        @AllArgsConstructor
        private static class PersonForm {
            @NotBlank
            String name;
            @NotBlank
            String buyingFor;
        }
    }

    @Getter
    @Setter
    @ToString
    @Entity
    public static class PersonEntity {
        @Id
        @GeneratedValue()
        Long id;

        String name;

        String buyingFor;

        @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
        List<ItemEntity> items;

        public PersonEntity() {
            id = null;
            items = new ArrayList<>();
            name = "";
        }
    }

    @Getter
    @Setter
    @ToString
    @Entity
    public static class ItemEntity {
        @Id
        @GeneratedValue
        Long id;
        String description;

        public ItemEntity() {
            id = null;
            description = "";
        }
    }
}
