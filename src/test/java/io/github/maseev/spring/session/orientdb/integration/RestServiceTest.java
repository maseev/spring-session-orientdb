package io.github.maseev.spring.session.orientdb.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfiguration.class)
public class RestServiceTest {

  private MockMvc mockMvc;

  @Before
  public void before() {
    mockMvc = MockMvcBuilders.standaloneSetup(new TestRestService()).build();
  }

  @Test
  public void basicSanityCheck() throws Exception {
    final String name = "John";
    final String firstExpectedResponse = "Hello, " + name;
    final String secondExpectedResponse = "Hello again, " + name;

    final MvcResult result = mockMvc.perform(get("/api/hello/{name}", name))
      .andExpect(status().isOk())
      .andExpect(content().string(firstExpectedResponse))
      .andReturn();
    final MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

    mockMvc.perform(get("/api/hello/{name}", name).session(session))
      .andExpect(status().isOk())
      .andExpect(content().string(secondExpectedResponse));
  }
}
