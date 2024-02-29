package com.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//exampleIterable();
		//exampleFlatMap();
		//exampleUserToString();
		//exampleCollectionList();
		//exampleUserCommentFlatMap();
		//exampleUserCommentZipWith();
		//exampleZipWithRange();
		//exampleInternal();
		//exampleDelayElement();
		exampleCreateFlux();
	}
	
	public void exampleCreateFlux() {
		Flux.create(emitter -> {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				private Integer count  = 0;
				@Override
				public void run() {
					emitter.next(++count);
					if(count == 10) {
						timer.cancel();
						emitter.complete();
					}
				}
			}, 1000, 1000);
		})
		.doOnNext(next -> log.info(next.toString()))
		.doOnComplete(() -> log.info("The process has finished"))
		.subscribe();
		
	}
	
	public void exampleDelayElement() {
		final Flux<Integer> range = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));
		
		range.blockLast();
	}
	
	public void exampleInternal() {
		final Flux<Integer> range = Flux.range(1, 12);
		final Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));
		
		range.zipWith(delay, (r, d) -> r)
		.doOnNext(i -> log.info(i.toString()))
		.blockLast();
	}
	
	
	private void exampleZipWithRange() {
		Flux.just(1,2,3,4)
		.map(i -> (i *2))
		.zipWith(Flux.range(0, 4), (first, second) -> String.format("Fist: %d, Second: %d", first, second))
		.subscribe(t -> log.info(t));
	}
	
	private void exampleUserCommentZipWith() {
		final Mono<User> userMono = Mono.fromCallable(() -> new User("Emilia", "Coraglio"));
		
		final Mono<Comment> userCommentMono = Mono.fromCallable(() -> {
			final Comment comment = new Comment();
			comment.add("This is a new comment");
			comment.add("What do you think about Spring?");
			return comment;
		});
		
		userMono.zipWith(userCommentMono, (user, comment) -> new UserComment(user, comment))
		.subscribe(uc -> log.info(uc.toString()));
	}
	
	private void exampleUserCommentFlatMap() {
		final Mono<User> userMono = Mono.fromCallable(() -> new User("Emilia", "Coraglio"));
		
		final Mono<Comment> userCommentMono = Mono.fromCallable(() -> {
			final Comment comment = new Comment();
			comment.add("This is a new comment");
			comment.add("What do you think about Spring?");
			return comment;
		});
		
		userMono.flatMap(user -> userCommentMono.map(comment -> new UserComment(user, comment)))
		.subscribe(uc -> log.info(uc.toString()));
	}
	
	private void exampleIterable() throws Exception {
		final List<String> userNames = new ArrayList<>();
		userNames.add("Mateo Rivero");
		userNames.add("Emilia Coraglio");
		userNames.add("Genaro Coraglio");
		userNames.add("Bruno Rivero");
		userNames.add("Juan Perez");
		
		final Flux<String> names = Flux.fromIterable(userNames);
		
		//Flux.just("Mateo Rivero", "Emilia Coraglio", "Gena Coragio", "Bruno Rivero", "Juan Perez")
		
		final Flux<User> users = names 
				.map(name -> {
					final String[] data = name.split(" ");
					return new User(data[0].toUpperCase(), data[1].toUpperCase());
					
				})
				.filter(user -> user.getLastName().equals("RIVERO"))
				.doOnNext(user -> {
					if(user == null || user.getFirstName().isEmpty() ) {
						throw new RuntimeException("Name must not be empty");
					}
					System.out.println(user.getFirstName());
				})
				.map(user -> new User(user.getFirstName().toLowerCase(), user.getLastName().toLowerCase()));

			users.subscribe(
				user -> System.out.println(user.toString()), 
				error -> log.error(error.getMessage()),
				new Runnable() {
					public void run() {
						log.info("The execution is completed");
					}
				}
				
		);
	}
	
	private void exampleFlatMap() throws Exception {
		final List<String> userNames = new ArrayList<>();
		userNames.add("Mateo Rivero");
		userNames.add("Emilia Coraglio");
		userNames.add("Genaro Coraglio");
		userNames.add("Bruno Rivero");
		userNames.add("Juan Perez");
		
		Flux.fromIterable(userNames)
				.map(name -> {
					final String[] data = name.split(" ");
					return new User(data[0].toUpperCase(), data[1].toUpperCase());
					
				})
				.flatMap(user -> {
					if(user.getLastName().equalsIgnoreCase("rivero")) {
						return Mono.just(user);
					} else {
						return Mono.empty();
					}
				})
				.map(user -> new User(user.getFirstName().toLowerCase(), user.getLastName().toLowerCase()))
				.subscribe(
						user -> System.out.println(user.toString())
				);
	}
	
	private void exampleUserToString() throws Exception {
		final List<User> users = new ArrayList<>();
		users.add(new User("Mateo", "Rivero"));
		users.add(new User("Bruno", "Rivero"));
		users.add(new User("Emilia", "Coraglio"));
		users.add(new User("Genaro", "Coraglio"));
		users.add(new User("Juan", "Perez"));
		
		Flux.fromIterable(users)
				.map(user -> user.getFirstName().concat(" ").concat(user.getLastName()))
				.flatMap(name -> {
					if(name.toLowerCase().contains("rivero")) {
						return Mono.just(name);
					} else {
						return Mono.empty();
					}
				})
				.map(String::toUpperCase)
				.subscribe(System.out::println);
	}
	
	private void exampleCollectionList() throws Exception {
		final List<User> users = new ArrayList<>();
		users.add(new User("Mateo", "Rivero"));
		users.add(new User("Bruno", "Rivero"));
		users.add(new User("Emilia", "Coraglio"));
		users.add(new User("Genaro", "Coraglio"));
		users.add(new User("Juan", "Perez"));
		
		Flux.fromIterable(users)
			.collectList()
			.subscribe(System.out::println);
	}


}
