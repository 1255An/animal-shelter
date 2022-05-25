package pro.sky.java.course7.animalshelter.service;

import pro.sky.java.course7.animalshelter.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserService {

    User save(User user, long chatId);

    Optional<User> parse(String userDataMessage);

    User getUserByChatId(long chatId);

    void deleteUserByChatId(long chatId);

//    Collection<User> getAllUsers();

}
