package hello.spring_item_service.validation;

import hello.spring_item_service.domain.item.Item;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class BeanValidationTest {

    @Test
    void beanValidationTest(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//      PLUS : 검증기 생성.
        Validator validator = factory.getValidator();

        Item item = new Item();
        item.setItemName("    ");
        item.setPrice(0);
        item.setQuantity(10000);

        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        for (ConstraintViolation<Item> violation : violations) {
//          PLUS : 오류 메세지는 Validation 기본 제공 메세지.
            System.out.println(violation.getMessage());
        }
    }
}
