package com.wallet.main.wallettracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class CustomRequestValidationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    String method = request.getMethod();
    String path = request.getRequestURI();

    // Example validation: Reject requests with invalid characters
    if (!isValidMethod(method) || !isValidPath(path)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isValidMethod(String method) {
    return method.matches("^[A-Z]+$");
  }

  private boolean isValidPath(String path) {
    // Add path validation logic if needed
    return true;
  }

}
