package hello.itemservice.validation;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.ObjectError;

public class MessageCodesResolverTest {

    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();

    @Test
    void messageCodesResolverObject() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        assertThat(messageCodes).containsExactly("required.item", "required");
    }

    @Test
    void messageCodesResolverFiled() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName",
            String.class);
        for (String messageCode : messageCodes) {
            System.out.println("messageCode = " + messageCode);
        }
        assertThat(messageCodes).containsExactly(
            "required.item.itemName",
            "required.itemName",
            "required.java.lang.String",
            "required"
        );

        /**
         * 필드 오류인 경우 아래 4가지 순서로 메시지 코드 생성
         * 1. code + "." + object name + "." + field
         * 2. code + "." + field
         * 3. code + "." + field Type
         * 4. code
         */
    }
}
