package com.example.recipeplatform.service;

import com.example.recipeplatform.dto.UserCreateDto;
import com.example.recipeplatform.dto.UserDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.UserMapper;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final String USER_WITH_ID_PREFIX = "User with id ";
    private static final String NOT_FOUND_SUFFIX = " was not found";
    private static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    private static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    private static final Sort SORT_BY_ID = Sort.by(Sort.Direction.ASC, "id");

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userMapper.toDtoList(userRepository.findAll(SORT_BY_ID));
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return userMapper.toDto(findEntity(id));
    }

    @Transactional
    public UserDto create(UserCreateDto dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException(USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException(EMAIL_ALREADY_EXISTS);
        }
        return userMapper.toDto(userRepository.save(userMapper.toEntity(dto)));
    }

    @Transactional
    public UserDto update(Long id, UserCreateDto dto) {
        User user = findEntity(id);
        validateUniqueFields(id, dto);
        userMapper.updateEntity(user, dto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(findEntity(id));
    }

    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX));
    }

    private void validateUniqueFields(Long currentId, UserCreateDto dto) {
        userRepository.findByUsernameIgnoreCase(dto.getUsername())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(USERNAME_ALREADY_EXISTS);
                });
        userRepository.findByEmailIgnoreCase(dto.getEmail())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(EMAIL_ALREADY_EXISTS);
                });
    }
}
