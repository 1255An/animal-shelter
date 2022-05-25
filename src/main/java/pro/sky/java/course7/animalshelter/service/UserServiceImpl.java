package pro.sky.java.course7.animalshelter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.java.course7.animalshelter.model.User;
import pro.sky.java.course7.animalshelter.repository.UserRepository;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String REGEX_BOT_MESSAGE = "([\\W+]+)(\\s)(\\+7\\d{3}[-.]?\\d{3}[-.]?\\d{4})(\\s)([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)";
    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Save created user in repository
     * @param user - created user
     * @param chatId - user's chat id
     * @return savedUser - user's data which was saved in repository
     */

    @Override
    public User save(User user, long chatId) {
        user.setChatId(chatId);
        User savedUser = repository.save(user);
            logger.info("Client's data has been saved successfully: " + savedUser);
        return savedUser;
    }

    /**
     * Parsing user's data to name, phone number, email
     * @param userDataMessage received message from user
     * @return parsing result
     */

    @Override
    public Optional<User> parse(String userDataMessage) {
        logger.info("Parsing method has been called");
        Pattern pattern = Pattern.compile(REGEX_BOT_MESSAGE);
        Matcher matcher = pattern.matcher(userDataMessage);
        User result = null;
        try {
            if (matcher.find()) {
                String name = matcher.group(1);
                String phoneNumber = matcher.group(3);
                String email = matcher.group(5);
                result = new User(name, phoneNumber, email);
            }
        } catch (Exception e) {
            logger.error("Failed to parse user's data: " + userDataMessage, e);
        }
        return Optional.ofNullable(result);
    }

    @Override
    public User getUserByChatId(long chatId) {
        logger.info("Was invoked method to find a student by chatId");
        return repository.findUserByChatId(chatId);
    }


    @Override
    public void deleteUserByChatId(long chatId) {
        logger.info("Was invoked method to delete a client by Id");
        repository.deleteById(repository.findUserByChatId(chatId).getId());
    }

//    @Override
//    public Collection<User> getAllUsers() {
//        logger.info("Was invoked method to get a list of all users");
//        return repository.findAll();
//    }
}
