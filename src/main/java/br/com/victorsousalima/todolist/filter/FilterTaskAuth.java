package br.com.victorsousalima.todolist.filter;


import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.victorsousalima.todolist.user.IUserRepository;
import br.com.victorsousalima.todolist.user.UserModel;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String serveletPath = request.getServletPath();

        if (serveletPath.startsWith("/tasks/")) {

            //Pegar a autenticação
            String authorization = request.getHeader("Authorization");
            String[] auth = authorization.split(" ");
            String authEncoded = auth[1];

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

            String authString = new String(authDecoded);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            UserModel userExists = this.userRepository.findByUsername(username);

            if (userExists == null) {
                response.sendError(401);
            } else {
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), userExists.getPassword());

                if (passwordVerify.verified) {
                    request.setAttribute("idUser", userExists.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        }
        else {
            filterChain.doFilter(request, response);
        }
    }
}
