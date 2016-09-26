package io.github.maseev.spring.session.orientdb.integration;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(path = "/api")
public class TestRestService {

  @RequestMapping(path = "/hello/{name}", method = GET)
  public String sayHello(@PathVariable final String name, HttpServletRequest request) {
    final HttpSession session = request.getSession();
    final Integer n = (Integer) session.getAttribute(name);

    if (n == null) {
      request.getSession().setAttribute(name, 1);
      return "Hello, " + name;
    } else if (n > 0) {
      request.getSession().setAttribute(name, n + 1);
      return "Hello again, " + name;
    } else {
      return "We don't know you";
    }
  }
}
