## 12.07 공부내용
### 1. BindingResult를 활용한 검증 오류 처리

파라미터에 BindingResult를 추가하여 FieldError, ObjectError처리 가능

```
if (!StringUtils.hasText(item.getItemName())) {
   bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
}
```
```
if (item.getPrice() != null && item.getQuantity() != null) {
   int resultPrice = item.getPrice() * item.getQuantity();
   if (resultPrice < 10000) {
      bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
   }
}
```
   
   **단, BindingResult 파라미터 위치는 @ModelAttribute Object oj 다음에 와야한다.**

#### 타임리프 스프링 검증 통합 기능

- 글로벌 오류처리
```
<div th:if="${#fields.hasGlobalErrors()}">
   <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">전체 오류 메시지</p>
</div>
```

- 필드 오류처리
```
<input type="text" id="itemName" th:field="*{itemName}" th:errorclass="field-error" class="form-control" placeholder="이름을 입력하세요">
   <div class="field-error" th:errors="*{itemName}"> 상품명 오류
</div>
```

### 2. properties 파일을 이용한 검증 오류처리

- errors.properties
```
range.item.price=가격은 {0} ~ {1} 까지 허용합니다.
```
```
if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
   bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
}
```

### 3. MessageCodeResolver를 이용한 오류코드 관리

**구체적인 것을 먼저 만들고, 덜 구체적인 것을 나중에 만들기!**

```
#Level1
required.item.itemName=상품 이름은 필수입니다.

#Level3
required.java.lang.String = 필수 문자입니다.

#Level4
required = 필수 값 입니다.
```

required에서 오류가 발생된다면 다음 순서대로 코드 메시지가 생성
1. `required.item.itemName`
2. `required.itemName`
3. `required.java.lang.String`
4. `required`

-----

-----
## 12.15 공부내용
### 1. BeanValidation 어노테이션을 활용한 검증 오류처리

build.gradle에 아래의 의존성 추가
```
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

BeanValidation 어노테이션의 MessageCodeResolver 코드메시지

@NotBlank에서 오류가 발생된다면 다음 순서대로 코드 메시지가 생성
1. `NotBlank.item.itemName`
2. `NotBlank.itemName`
3. `NotBlank.java.lang.String` 
4. `NotBlank`

### 2. BeanValidation에서 필드 오류가 아닌 오브젝트 오류 처리
@ScriptAssert 어노테이션을 활용하여 처리가능하나 오브젝트 오류는 자바코드로 제어하는 것을 권장
```
@ScriptAssert(lang = "javascript", script ="_this.price * _this.quantity >= 10000", message = "가격 * 수량은 10,000 이상이어야 합니다.")
```

### 3. BeanValidation의 한계
등록시와 수정시의 요구사항이 다르면 BeanValidation 어노테이션만으로 제어가 불가하다.

Ex) 등록시 수량은 최대 9,999개 였으나 수정시에는 제한이 없는 경우

**위의 문제를 해결하는 2가지 방법**
1. BeanValidation의 groups 기능 사용

    1-1. 저장용, 수정용 groups 인터페이스 생성
    
    1-2. BeanValidation 어노테이션에 groups 적용
    ```
    @NotNull(groups = UpdateCheck.class) //수정시에만 적용
    private Long id;
    
   @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;
    ```
    1-3. 저장, 수정 메서드에 groups 적용
    ```
   @PostMapping("/add")
    public String addItemV2(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        //...
    }
   
   @PostMapping("/{itemId}/edit")
    public String editV2(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {
        //... 
    }
   ```
2. Item 객체를 직접 사용하지 않고 저장 폼객체, 수정 폼객체를 각각 만들어 사용

   HTML Form -> ItemSaveForm -> Controller -> Item 생성 -> Repository

**주의할점** 

생성, 수정 메서드내 파라미터를 폼객체로 변경시 파라미터명이 변경되었다면 @ModelAttribute("item")으로
넣어주도록 한다.

추가하지 않을시 itemSaveForm과 같은 명으로 model에 담기게 되어 뷰 템플릿에서 접근이 불가하다.

### 4. @RequestBody에서 @Validated를 이용한 오류 처리
API의 경우 3가지 경우가 있으나 3번째 실패 요청의 경우 Validator가 적용되지 않는다.

- 성공 요청: 성공
- 검증 오류 요청: JSON을 객체로 생성하는 것은 성공했고, 검증에서 실패함
- <span style="color:red"> 실패 요청: JSON을 객체로 생성하는 것 자체가 실패함 </span>

#### @ModelAttribute VS @RequestBody
- `@ModelAttribute` 는 필드 단위로 정교하게 바인딩이 적용된다. 특정 필드가 바인딩 되지 않아도 나머지 필드
는 정상 바인딩 되고, Validator를 사용한 검증도 적용할 수 있다.
- `@RequestBody` 는 HttpMessageConverter 단계에서 JSON 데이터를 객체로 변경하지 못하면 이후 단계 자
체가 진행되지 않고 예외가 발생한다. 컨트롤러도 호출되지 않고, Validator도 적용할 수 없다.