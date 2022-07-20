package com.cosmos.aad;

import com.cosmos.aad.sync.User;
import com.cosmos.aad.sync.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        //
        System.out.println("\n\n\n\nmain invoked \n\n\n\n");

    }

    public void run(String... var1) {
        System.out.println("\n\n\n\n rund invoked \n\n\n\n");
        //
        final User testUser1 = new User("testId1", "testFirstName", "testLastName1");
        final User testUser2 = new User("testId2", "testFirstName", "testLastName2");
        // <Delete>
        userRepository.deleteAll();
        //
        System.out.println("Save the users");
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        final User result = userRepository.findByIdAndLastName(testUser1.getId(), testUser1.getLastName());
        System.out.println(result);
    }
}