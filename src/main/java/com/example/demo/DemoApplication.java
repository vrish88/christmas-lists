package com.example.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
            mv.addObject("persons", repo.findAll());
            return mv;
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
        public String createPerson(PersonEntity personEntity) {
            repo.save(personEntity);
            return "redirect:/person/" + personEntity.getId();
        }

        @GetMapping("/person/{id}")
        public String getPerson(@PathVariable("id") Long id, Model model) {
            Optional<PersonEntity> byId = repo.findById(id);

            model.addAttribute("person", byId.get());
            return "person-show";
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

        @OneToMany(cascade = CascadeType.ALL)
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
