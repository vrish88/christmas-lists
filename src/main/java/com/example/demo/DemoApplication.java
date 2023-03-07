package com.example.demo;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    @Profile("cloud")
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests()
            .anyRequest().authenticated().and()
            .oauth2Login().and()
            .build();
    }

    @Bean
    @Profile("!cloud")
    public SecurityFilterChain noAuthFilterChain(HttpSecurity http) throws Exception {
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
            return new IndexView(repo.findAll());
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
        public ModelAndView removeListItem(
            @PathVariable("personId") Long personId,
            @PathVariable("itemId") Long itemId
        ) {
            PersonEntity personEntity = repo.findById(personId).get();
            personEntity.getItems().removeIf(i -> i.getId().equals(itemId));
            repo.save(personEntity);

            ModelAndView mv = new ModelAndView("person-show");
            mv.addObject("person", personEntity);
            return mv;
        }

        @PostMapping("/person")
        public ModelAndView createPerson(
            @Valid PersonForm personForm,
            BindingResult bindingResult,
            HttpServletResponse response
        ) {
            if (bindingResult.hasErrors()) {
                ModelAndView view = new PersonFormView(repo.findAll(), personForm);
                view.setStatus(HttpStatusCode.valueOf(422));
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

        private static class IndexView extends ModelAndView {
            public IndexView(Iterable<PersonEntity> persons) {
                super("index");
                this.addObject("persons", persons);
                PersonFormView personFormView = new PersonFormView(persons, new PersonForm("", ""));
                this.addAllObjects(personFormView.getModel());
            }
        }

        private static class PersonFormView extends ModelAndView {
            public PersonFormView(Iterable<PersonEntity> persons, PersonForm personForm) {
                super("person-form");
                this.addObject("persons", persons);
                this.addObject("personForm", personForm);
            }
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
