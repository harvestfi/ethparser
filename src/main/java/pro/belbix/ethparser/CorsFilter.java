package pro.belbix.ethparser;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorsFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(@NotNull final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain) throws ServletException, IOException {
    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Allow-Methods",
        "GET, POST, DELETE, PUT, PATCH, HEAD");
    response.addHeader("Access-Control-Allow-Headers",
        "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
    response.addHeader("Access-Control-Expose-Headers",
        "Access-Control-Allow-Origin, Access-Control-Allow-Credentials");
    response.addHeader("Access-Control-Allow-Credentials", "true");
    response.addIntHeader("Access-Control-Max-Age", 3600);
    filterChain.doFilter(request, response);
  }

}
