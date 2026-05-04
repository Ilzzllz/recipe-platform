package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.RecipeQueryCacheService;
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
    private static final String USER_NOT_EMPTY = "Cannot delete user that has recipes. Remove or reassign recipes first.";
    private static final Sort SORT_BY_ID = Sort.by(Sort.Direction.ASC, "id");

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RecipeQueryCacheService recipeQueryCacheService;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                       RecipeQueryCacheService recipeQueryCacheService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.recipeQueryCacheService = recipeQueryCacheService;
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
        String oldUsername = user.getUsername();
        validateUniqueFields(id, dto);
        userMapper.updateEntity(user, dto);
        UserDto result = userMapper.toDto(userRepository.save(user));
        if (!oldUsername.equalsIgnoreCase(dto.getUsername())) {
            recipeQueryCacheService.invalidateAll();
        }
        return result;
    }

    @Transactional
    public void delete(Long id) {
        User user = findEntity(id);
        if (!user.getRecipes().isEmpty()) {
            throw new IllegalArgumentException(USER_NOT_EMPTY);
        }
        userRepository.delete(user);
        recipeQueryCacheService.invalidateAll();
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