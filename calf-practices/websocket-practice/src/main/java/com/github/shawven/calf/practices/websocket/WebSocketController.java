package com.github.shawven.calf.practices.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Date;
import java.util.Map;

/**
 * @author FS
 * @date 2018-09-30 10;
 */
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("ws")
    public String ws() {
        return "index";
    }

    @MessageMapping("/question")
    @SendToUser("/answer")
    public Message handleQuestion(Message message,
                                  Principal principal,
                                  @Headers Map<String, Object> headers,
                                  HttpSession session,
                                  SimpMessageHeaderAccessor headerAccessor) throws Exception {
        simpMessagingTemplate.convertAndSendToUser("abc", "/answer", message);
        return message;
    }

    @MessageExceptionHandler
    @SendToUser("/errors")
    public String handleException(Exception ex) {
        ex.printStackTrace();
        return ex.getMessage();
    }

    public static class Message {
        private Integer formUser;
        private Integer toUser;
        private String command;
        private Body body;
        private Date date;

        public Integer getFormUser() {
            return formUser;
        }

        public void setFormUser(Integer formUser) {
            this.formUser = formUser;
        }

        public Integer getToUser() {
            return toUser;
        }

        public void setToUser(Integer toUser) {
            this.toUser = toUser;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public Body getBody() {
            return body;
        }

        public void setBody(Body body) {
            this.body = body;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "formUser=" + formUser +
                    ", toUser=" + toUser +
                    ", command='" + command + '\'' +
                    ", body=" + body +
                    ", date=" + date +
                    '}';
        }

        public static class Body {
            private Integer id;
            private Integer type;
            private String subject;
            private String content;
            private String images;

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            public Integer getType() {
                return type;
            }

            public void setType(Integer type) {
                this.type = type;
            }

            public String getSubject() {
                return subject;
            }

            public void setSubject(String subject) {
                this.subject = subject;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getImages() {
                return images;
            }

            public void setImages(String images) {
                this.images = images;
            }

            @Override
            public String toString() {
                return "Body{" +
                        "id=" + id +
                        ", type=" + type +
                        ", subject='" + subject + '\'' +
                        ", content='" + content + '\'' +
                        ", images='" + images + '\'' +
                        '}';
            }
        }
    }
}
