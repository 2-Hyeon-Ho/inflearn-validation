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