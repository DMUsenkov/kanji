package ru.hse;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TelegramBotDataStubs {

    private int fakeUpdateId = 0;
    private int fakeMessageId = 0;
    private Map<Long, User> fakeUsers = new HashMap<>();
    private Map<Long, Chat> fakeChats = new HashMap<>();

    public Update formUpdateRequest(String s, long i) {
        return new Update(fakeUpdateId++,
                formMessage(s, i), null,null, null,
                null, null,null, null,
                null, null,null, null,
                null, null);
    }

    private Message formMessage(String s, long i) {
        return new Message(fakeMessageId++,
                (int)i,
                formFakeUser(i),
                (int)new Date().getTime(),
                formFakeChat(i),null,null,null,s,
                null, null, null,null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,null,null);
    }
    private User formFakeUser(long i){
        User u = fakeUsers.get(i);
        if(u == null){
            u = new User(i, "USER_"+i, false);
            fakeUsers.put(i, u);
        }
        return u;
    }
    private Chat formFakeChat(long i){
        Chat u = fakeChats.get(i);
        if(u == null){
            u = new Chat(i, "USER_"+i);
            fakeChats.put(i, u);
        }
        return u;
    }
}
