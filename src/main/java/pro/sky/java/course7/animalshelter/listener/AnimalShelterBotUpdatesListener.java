package pro.sky.java.course7.animalshelter.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.java.course7.animalshelter.model.User;
import pro.sky.java.course7.animalshelter.service.UserService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import static pro.sky.java.course7.animalshelter.service.Constants.*;


@Service
public class AnimalShelterBotUpdatesListener implements UpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(AnimalShelterBotUpdatesListener.class);
    private static boolean registrationDataSent =false;

    private final TelegramBot animalShelterBot;
    private final UserService userService;

    public AnimalShelterBotUpdatesListener(TelegramBot animalShelterBot, UserService userService) {
        this.animalShelterBot = animalShelterBot;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        animalShelterBot.setUpdatesListener(this);
    }


    /**
     * Check and process chat's updates
     *
     * @param updates used for receiving and checking different types of updates
     * @return confirmed all updates
     */

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Message message = update.message();
            // check if the update has a message and message has text
            if (message != null && message.text() != null) {
                String inputMessage = message.text();
                handleMessage(inputMessage, extractChatId(message));
            } else if (update.callbackQuery().data() != null) {
                logger.info("Callback: " + update.callbackQuery().data() + " has been received");
                handleMessage(update.callbackQuery().data(), update.callbackQuery().from().id());
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    /**
     * Recognize and implement user's messages
     *
     * @param inputMessage - user's message sent to bot
     * @param chatId       - user's chat id
     */

    private void handleMessage(String inputMessage, long chatId) {
        SendMessage outputMessage;
        switch (inputMessage) {
            case START_CMD:
                logger.info(START_CMD + " message has been received");
                outputMessage = new SendMessage(chatId, GREETINGS_TEXT)
                        // show start menu
                        .replyMarkup(startButtons());
                break;
            case SHELTER_INFO_CMD:
                logger.info(SHELTER_INFO_CMD + " message has been received");
                outputMessage = new SendMessage(chatId, "О приюте")
                        // show "About shelter" menu
                        .replyMarkup(infoButtons());
                break;
            case BACK_TO_START_TEXT:
                logger.info(BACK_TO_START_CMD + " message has been received");
                outputMessage = new SendMessage(chatId, BACK_TO_START_TEXT)
                        // back to start menu
                        .replyMarkup(startButtons());
                break;
            case ABOUT_US_CMD:
                logger.info(ABOUT_US_CMD + " message has been received");
                outputMessage = new SendMessage(chatId, ABOUT_US_TEXT);
                break;
            case WORKING_HOURS_CMD:
                logger.info(WORKING_HOURS_CMD + " message has been received");
                outputMessage = new SendMessage(chatId, WORKING_HOURS_TEXT);
                break;
            case SAFETY_RECOMMENDATION_CMD:
                logger.info(SAFETY_RECOMMENDATION_CMD + " message has been received");
                outputMessage = new SendMessage(chatId, SAFETY_RECOMMENDATION_TEXT);
                break;
            case CONTACT_ME_CMD:
                logger.info(CONTACT_ME_CMD + " message has been received");
                outputMessage = new SendMessage(chatId, CONTACT_ME_TEXT);
                registrationDataSent = true;
                break;
            default:
                if (registrationDataSent) {
                    logger.info("Registration data has been sent");
                    outputMessage = registrationUser(inputMessage, chatId);
                } else {
                    logger.info(INVALID_NOTIFICATION_OR_CMD + " message has been received");
                 outputMessage = new SendMessage(chatId, INVALID_NOTIFICATION_OR_CMD);
                }
        }
        try {
            animalShelterBot.execute(outputMessage);
        } catch (Exception e) {
            logger.info("Exception was thrown in handle message method ");
            e.printStackTrace();
        }
    }

    //    private static ReplyKeyboardMarkup startButtons() {
//        return new ReplyKeyboardMarkup(
//                new String[]{"О приюте", "Как забрать питомца"},
//                new String[]{"Прислать отчет", "Позвать волонтера"})
//                .oneTimeKeyboard(true)
//                .resizeKeyboard(true)
//                .selective(true);
//    }

    /**
     * Create fixed buttons
     *
     * @return buttons
     */

    private static Keyboard startButtons() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("О приюте"),
                        new KeyboardButton("Как забрать питомца"),
                },
                new KeyboardButton[]{
                        new KeyboardButton("Прислать отчет"),
                        new KeyboardButton("Позвать волонтера"),
                })
                .resizeKeyboard(true);
    }

    /**
     * Create "About shelter" menu with buttons
     *
     * @return buttons
     */

    private static InlineKeyboardMarkup infoButtons() {
        logger.info("Inline Keyboard was called");
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Кто мы").callbackData(ABOUT_US_CMD),
                        new InlineKeyboardButton("Адрес").callbackData(WORKING_HOURS_CMD)
                },
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Безопасность").callbackData(SAFETY_RECOMMENDATION_CMD),
                        new InlineKeyboardButton("Связаться со мной").callbackData(CONTACT_ME_CMD)

                });
    }

    /**
     * Define user's chat id
     *
     * @param message - user's message sent to bot
     * @return chat id number
     */

    private long extractChatId(Message message) {
        return message.chat().id();
    }

    /**
     * Save parsing result of user's data
     *
     * @param inputMessage - user's message with his data
     * @param chatId       - chat id number
     * @return message from bot to user
     */

    private SendMessage registrationUser(String inputMessage, Long chatId) {
        SendMessage outputMessage;
        Optional<User> parseResult = userService.parse(inputMessage);
        if (parseResult.isPresent()) {
            if (userService.getUserByChatId(chatId) == null) {
                logger.info("Parse result is valid");
                userService.save(parseResult.get(), chatId);
                outputMessage = new SendMessage(chatId, SUCCESS_SAVING_TEXT);
            } else {
                logger.info("Data is already exists, it will be restored");
                userService.deleteUserByChatId(chatId);
                userService.save(parseResult.get(), chatId);
                outputMessage = new SendMessage(chatId, "Ваши данные успешно перезаписаны" );
            }
        } else {
            logger.info("Invalid registration data");
            outputMessage = new SendMessage(chatId, CONTACT_ME_TEXT );
        }
        return outputMessage;
    }

}

