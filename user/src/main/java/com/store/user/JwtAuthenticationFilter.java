package com.store.user;

import com.store.user.JwtTokenProvider;
import com.store.user.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    // Constructor-based dependency injection (lebih aman daripada @Autowired pada field)
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Ambil header Authorization dari request
        String header = request.getHeader("Authorization");

        // Pastikan header mengandung Bearer token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);  // Potong prefix "Bearer "

            try {
                // Validasi token dan ambil username dari token
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);

                    // Ambil detail user berdasarkan username
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Buat objek UsernamePasswordAuthenticationToken
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Tambahkan detail tambahan ke authentication
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication di SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Log error jika terjadi kesalahan dalam validasi token atau otentikasi
                System.out.println("Cannot set user authentication: " + e.getMessage());
            }
        }

        // Lanjutkan filter chain
        filterChain.doFilter(request, response);
    }
}