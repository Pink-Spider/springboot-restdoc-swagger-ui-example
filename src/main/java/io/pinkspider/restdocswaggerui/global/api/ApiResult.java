package io.pinkspider.restdocswaggerui.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResult<T> {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Getter
    @Builder.Default
    private int code = ApiStatus.OK.getResultCode();

    @Getter
    @Builder.Default
    private String message = ApiStatus.OK.getResultMessage();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T value;

    public static ApiResult getBase() {
        return ApiResult.builder()
            .build();
    }

    public static <V> Map<String, V> getSingleResult(String key, V value) {
        Map<String, V> singleResult = new HashMap<>();
        singleResult.put(key, value);

        return singleResult;
    }

    public Object getValue() {
        return jsonMapper.readValue(jsonMapper.writeValueAsString(this.value), Object.class);
    }
}
