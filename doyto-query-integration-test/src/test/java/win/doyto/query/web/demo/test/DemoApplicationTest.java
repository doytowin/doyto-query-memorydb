package win.doyto.query.web.demo.test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.web.demo.DemoApplication;
import win.doyto.query.web.response.ErrorCode;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DemoApplicationTest
 *
 * @author f0rb
 */
@Transactional
@ActiveProfiles("test")
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
abstract class DemoApplicationTest {

    @Resource
    protected MockMvc mockMvc;

    protected ResultActions performAndExpectSuccess(RequestBuilder requestBuilder) throws Exception {
        return performAndExpectOk(requestBuilder)
                .andExpect(jsonPath("$.success").value(true));
    }

    protected ResultActions performAndExpectOk(RequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder)
                      .andDo(print())
                      .andExpect(status().isOk());
    }

    protected MockHttpSession session = new MockHttpSession();

    protected ResultActions performAndExpectSuccess(MockHttpServletRequestBuilder builder, String content) throws Exception {
        return performAndExpectSuccess(buildJson(builder, content));
    }

    protected MockHttpServletRequestBuilder buildJson(MockHttpServletRequestBuilder builder, String content) {
        return builder.content(content).contentType("application/json;charset=UTF-8").session(session);
    }

    protected void performAndExpectFail(RequestBuilder requestBuilder, ErrorCode errorCode) throws Exception {
        performAndExpectOk(requestBuilder).andExpect(jsonPath("$.code").value(errorCode.getCode()));
    }

}