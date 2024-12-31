package com.store.user;

import com.store.user.User;
import com.store.user.UserDto;
import com.store.user.UserRole;
import com.store.user.UserRepository;
import com.store.user.UserAlreadyExistsException;
import com.store.user.InvalidRoleException;
import com.store.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerNewUser(UserDto userDto) {
        // Cek apakah username sudah ada
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        // Cek apakah email sudah ada
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        // Buat User baru
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Tetapkan role, default ke ROLE_USER
        if (userDto.getRole() != null) {
            try {
                user.setRole(UserRole.valueOf(userDto.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidRoleException("Invalid role specified");
            }
        } else {
            user.setRole(UserRole.ROLE_USER);
        }

        user.setEnabled(true);

        // Simpan user
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Pastikan username dan email tidak null atau kosong
        if (userDto.getUsername() != null && !userDto.getUsername().isEmpty()) {
            existingUser.setUsername(userDto.getUsername());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
            // Cek apakah email sudah digunakan oleh user lain
            if (userRepository.existsByEmail(userDto.getEmail()) && !existingUser.getEmail().equals(userDto.getEmail())) {
                throw new UserAlreadyExistsException("Email is already in use");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        // Perbarui password hanya jika password tidak kosong
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }
}
