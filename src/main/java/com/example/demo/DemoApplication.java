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
import org.springframework.web.bind.annotation.*;
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
	public ApplicationRunner runner(ChristmasListRepo repo) {
		return (args -> {
			ChristmasList list = new ChristmasList();
			list.setName("FIRST!");

			repo.save(list);
		} );
	}

	@Controller
	static class ChristmasListController {
		private final ChristmasListRepo repo;

		ChristmasListController(ChristmasListRepo repo) {
			this.repo = repo;
		}

		@GetMapping("/")
		public ModelAndView index() {
			ChristmasList list = new ChristmasList();
			list.setId(1L);
			ModelAndView mv = new ModelAndView("index");
			mv.addObject("list", list);
			return mv;
		}

		@PostMapping("/christmas-list/{listId}/items")
		public String createItemInList(@PathVariable("listId") Long listId, Item item) {
			ChristmasList list = repo.findById(listId).get();
			ArrayList<Item> items = new ArrayList<>(list.getItems());
			items.add(item);
			list.setItems(items);
			System.out.println(list);
			repo.save(list);

			return "redirect:/christmas-list/" + list.getId();
		}
		@DeleteMapping("/christmas-list/{listId}/items/{itemId}")
		public ModelAndView removeListItem(@PathVariable("listId") Long listId, @PathVariable("itemId") Long itemId) {
			ChristmasList list = repo.findById(listId).get();
			list.getItems().removeIf(i -> i.getId().equals(itemId));
			repo.save(list);

			ModelAndView mv = new ModelAndView("christmas-list-show");
			mv.addObject("list", list);
			return mv;
		}
		@PostMapping("/christmas-list")
		public String createChristmasList(ChristmasList list) {
			repo.save(list);
			return "redirect:/christmas-list/" + list.getId();
		}

		@GetMapping("/christmas-list/{id}")
		public String getChristmasList(@PathVariable("id") Long id, Model model) {
			Optional<ChristmasList> byId = repo.findById(id);

			model.addAttribute("list", byId.get());
			return "christmas-list-show";
		}
	}

	@Getter
	@Setter
	@ToString
	@Entity
	public static class ChristmasList {
		@Id
		@GeneratedValue()
		Long id;

		String name;

		@OneToMany(cascade = CascadeType.ALL)
		List<Item> items;

		public ChristmasList() {
			id = null;
			items = new ArrayList<>();
			name = "";
		}
	}

	@Getter
	@Setter
	@ToString
	@Entity
	public static class Item {
		@Id
		@GeneratedValue
		Long id;
		String description;

		public Item() {
			id = null;
			description = "";
		}
	}
}
