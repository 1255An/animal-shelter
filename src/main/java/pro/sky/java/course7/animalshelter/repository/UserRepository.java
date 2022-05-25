package pro.sky.java.course7.animalshelter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.java.course7.animalshelter.model.User;



public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByChatId(Long chatId);

    void deleteUserByChatId(Long chatId);




}
