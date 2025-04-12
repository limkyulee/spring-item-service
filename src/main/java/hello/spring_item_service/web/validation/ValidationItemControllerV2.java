package hello.spring_item_service.web.validation;

import hello.spring_item_service.domain.item.Item;
import hello.spring_item_service.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final View error;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

//  REFACTOR : BindingResult 사용하여 검증 로직 작성하도록 수정.
//  BindingResult 가 있을 경우, FieldError 를 BindingResult 에 담아서 컨트롤러를 정상 호출함.
//  BindingResult 가 없을 경우, 400 Error 발생 후 컨트롤러를 호출하지않음.
//  BindingResult 는 Model 에 자동으로 포함됨.
//  @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1_000_000){
           bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 혀용합니다."));
        }

        if(item.getQuantity() == null || item.getQuantity() > 9_999){
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10_000){
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 : " + resultPrice));
            }
        }

        // 검증에 실패한 경우 | 다시 view template 으로 이동.
        if(bindingResult.hasErrors()){
            log.info("error = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 검증에 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//  REFACTOR : 에러 발생 시에도 입력한 값이 남아있도록 로직 수정.
//    > rejectedValue 를 item 객체에서 조회한 값으로 설정. | 사용자의 입력값을 FieldError 가 가지고 있음.
//    > FieldError 의 두가지 타입.
//    public FieldError(String objectName, String field, String defaultMessage);
//    public FieldError(String objectName, String field, @Nullable Object rejectedValue,
//                      boolean bindingFailure, @Nullable String[] codes,
//                      @Nullable Object[] arguments, @Nullable String defaultMessage)
//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1_000_000){
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false,null,null, "가격은 1,000 ~ 1,000,000 까지 혀용합니다."));
        }

        if(item.getQuantity() == null || item.getQuantity() > 9_999){
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, null,null, "수량은 최대 9,999 까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10_000){
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 : " + resultPrice));
            }
        }

        // 검증에 실패한 경우 | 다시 view template 으로 이동.
        if(bindingResult.hasErrors()){
            log.info("error = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 검증에 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError(
                    "item", "itemName", item.getItemName(), false,
                    new String[]{"required.item.itemName"}, null, null)
            );
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1_000_000){
            bindingResult.addError(new FieldError(
                    "item", "price", item.getPrice(), false,
                    new String[]{"range.item.price"},new Object[]{1_000, 1_000_000}, null)
            );
        }

        if(item.getQuantity() == null || item.getQuantity() > 10000){
            bindingResult.addError(new FieldError(
                    "item", "quantity", item.getQuantity(), false,
                    new String[]{"max.item.quantity"},new Object[]{9_999}, null)
            );
        }

        // 특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10_000){
                bindingResult.addError(new ObjectError(
                        "item", new String[]{"totalPriceMin"},
                        new Object[]{10_000, resultPrice}, null)
                );
            }
        }

        // 검증에 실패한 경우 | 다시 view template 으로 이동.
        if(bindingResult.hasErrors()){
            log.info("error = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 검증에 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//  REFACTOR : rejectValue, reject 사용하도록 로직 수정.
//    > FieldError | rejectValue(@Nullable String field, String errorCode, @Nullable Object[] errorArgs, @Nullable String defaultMessage);
//    > ObjectError | reject(String errorCode, @Nullable Object[] errorArgs, @Nullable String defaultMessage);
    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        log.info("bindindResult : {}", bindingResult.getObjectName());
        log.info("bindingResult : {}", bindingResult.getTarget());

        // 검증 로직
        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");
//        if(!StringUtils.hasText(item.getItemName())){
//            bindingResult.rejectValue("itemName", "required"); // PLUS : errorCode 만 넣으면 object 명 + field 명 조합하여 찾음.
//        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        if(item.getQuantity() == null || item.getQuantity() > 10000){
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000){
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // 검증에 실패한 경우 | 다시 view template 으로 이동.
        if(bindingResult.hasErrors()){
            log.info("error = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 검증에 성공한 경우
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

