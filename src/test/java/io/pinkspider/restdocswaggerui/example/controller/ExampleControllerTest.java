package io.pinkspider.restdocswaggerui.example.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import io.pinkspider.restdocswaggerui.example.dto.ExampleRequestDto;
import io.pinkspider.restdocswaggerui.example.dto.ExampleResponseDto;
import io.pinkspider.restdocswaggerui.example.service.ExampleService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ExampleController.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class ExampleControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    @MockitoBean
    protected ExampleService exampleService;

    @Test
    void example01() throws Exception {
        // given
        ExampleRequestDto exampleRequestDto = ExampleRequestDto.builder()
            .name("name")
            .age(10)
            .hobbies(Arrays.asList("piano", "coding"))
            .subClassList(Collections.singletonList(
                    ExampleRequestDto.SubClass.builder()
                        .subName("subName")
                        .subAge(10)
                        .build()
                )
            )
            .build();

        ExampleResponseDto exampleResponseDto = ExampleResponseDto.builder()
            .name("name")
            .age(10)
            .hobbies(Arrays.asList("piano", "coding"))
            .subClassList(Collections.singletonList(
                    ExampleResponseDto.SubClass.builder()
                        .subName("subName")
                        .subAge(10)
                        .build()
                )
            )
            .build();

        String pathVariable = "pathVariable";

        when(exampleService.example(any()))
            .thenReturn(exampleResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.post("/example/{pathVariable}", "222")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(exampleRequestDto)))
            .andDo(
                MockMvcRestDocumentationWrapper.document("example-controller",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .description("Example API")
                            .summary("Example endpoint summary")
                            .tag("Example")
                            .pathParameters(
                                parameterWithName("pathVariable").description("Path variable description")
                            )
                            .requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("Name"),
                                fieldWithPath("age").type(JsonFieldType.NUMBER).description("Age"),
                                fieldWithPath("hobbies").type(JsonFieldType.ARRAY).description("Hobbies"),
                                fieldWithPath("subClassList[]").type(JsonFieldType.ARRAY).description("Subclasses"),
                                fieldWithPath("subClassList[].subName").type(JsonFieldType.STRING).description("Subclass name"),
                                fieldWithPath("subClassList[].subAge").type(JsonFieldType.NUMBER).description("Subclass age")
                            )
                            .responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("code"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("message"),
                                fieldWithPath("value").type(JsonFieldType.OBJECT).description("value"),
                                fieldWithPath("value.name").type(JsonFieldType.STRING).description("Name"),
                                fieldWithPath("value.age").type(JsonFieldType.NUMBER).description("Age"),
                                fieldWithPath("value.hobbies").type(JsonFieldType.ARRAY).description("Hobbies"),
                                fieldWithPath("value.subClassList[]").type(JsonFieldType.ARRAY).description("Subclasses"),
                                fieldWithPath("value.subClassList[].subName").type(JsonFieldType.STRING).description("Subclass name"),
                                fieldWithPath("value.subClassList[].subAge").type(JsonFieldType.NUMBER).description("Subclass age")
                            )
                            .build()
                    )
                )
            );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }
}
