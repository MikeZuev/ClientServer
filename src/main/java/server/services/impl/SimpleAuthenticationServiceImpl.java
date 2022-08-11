package server.services.impl;

import server.models.User;
import server.services.AuthenticationService;

import java.util.List;

public class SimpleAuthenticationServiceImpl implements AuthenticationService {

    private static final List<User> clients = List.of(
            new User("martin", "1", "George_Martin"),
            new User("batman","1","Bruce_Wayne"),
            new User("gena", "1", "Gandalf_TheGrey" ),
            new User("bob", "1", "SpongeBob"),
            new User("bender", "1", "Bender_TheRobot"),
            new User("mike", "1","Mike"),
            new User("bike", "1", "Bike")
    );

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User client : clients) {
            if(client.getLogin().equals(login) && client.getPassword().equals(password)){
                return client.getUsername();
            }

        }
        return null;
    }
}
