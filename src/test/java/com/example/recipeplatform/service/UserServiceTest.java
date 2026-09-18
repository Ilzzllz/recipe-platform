package com.example.recipeplatform.service;

import com.example.recipeplatform.cache.RecipeQueryCacheService;
import com.example.recipeplatform.dto.UserCreateDto;
import com.example.recipeplatform.dto.UserDto;
import com.example.recipeplatform.exception.NotFoundException;
import com.example.recipeplatform.mapper.UserMapper;
import com.example.recipeplatform.model.Recipe;
import com.example.recipeplatform.model.User;
import com.example.recipeplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RecipeQueryCacheService recipeQueryCacheService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, recipeQueryCacheService);
    }

    @Test
    @DisplayName("findAll should return sorted list of user DTOs")
    void findAllShouldReturnSortedDtoList() {
        User user = new User();
        UserDto dto = new UserDto();
        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(user));
        when(userMapper.toDtoList(List.of(user))).thenReturn(List.of(dto));

        List<UserDto> result = userService.findAll();

        assertThat(result).containsExactly(dto);
        verify(userRepository).findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    @DisplayName("getById should return user DTO when user exists")
    void getByIdShouldReturnUserDto() {
        User user = new User();
        UserDto dto = new UserDto();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.getById(1L);

        assertThat(result).isSameAs(dto);
    }

    @Test
    @DisplayName("getById should throw NotFoundException when user does not exist")
    void getByIdShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id 999 was not found");
    }

    @Test
    @DisplayName("create should save user and invalidate cache when username and email are unique")
    void createShouldSaveUserSuccessfully() {
        UserCreateDto request = sampleCreateDto("john", "john@example.com");
        User entity = new User();
        User savedEntity = new User();
        UserDto dto = new UserDto();

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(dto);

        UserDto result = userService.create(request);

        assertThat(result).isSameAs(dto);
        verify(userRepository).save(entity);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("create should throw IllegalArgumentException when username is taken")
    void createShouldThrowWhenUsernameExists() {
        UserCreateDto request = sampleCreateDto("duplicate", "john@example.com");
        when(userRepository.existsByUsernameIgnoreCase("duplicate")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw IllegalArgumentException when email is taken")
    void createShouldThrowWhenEmailExists() {
        UserCreateDto request = sampleCreateDto("unique", "taken@example.com");
        when(userRepository.existsByUsernameIgnoreCase("unique")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should update user when fields are unique or unchanged")
    void updateShouldUpdateUserSuccessfully() {
        User existing = new User();
        existing.setId(1L);
        UserCreateDto request = sampleCreateDto("john_new", "john_new@example.com");
        UserDto dto = new UserDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsernameIgnoreCase("john_new")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("john_new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(dto);

        UserDto result = userService.update(1L, request);

        assertThat(result).isSameAs(dto);
        verify(userMapper).updateEntity(existing, request);
        verify(userRepository).save(existing);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("update should allow keeping same username and email for same user id")
    void updateShouldAllowSameUserFields() {
        User existing = new User();
        existing.setId(1L);
        UserCreateDto request = sampleCreateDto("john", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(existing));
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(new UserDto());

        UserDto result = userService.update(1L, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("update should throw IllegalArgumentException when username is taken by another user")
    void updateShouldThrowWhenUsernameTakenByAnother() {
        User current = new User();
        current.setId(1L);
        User other = new User();
        other.setId(2L);
        UserCreateDto request = sampleCreateDto("other_user", "my_email@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(current));
        when(userRepository.findByUsernameIgnoreCase("other_user")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw IllegalArgumentException when email is taken by another user")
    void updateShouldThrowWhenEmailTakenByAnother() {
        User current = new User();
        current.setId(1L);
        User other = new User();
        other.setId(2L);
        UserCreateDto request = sampleCreateDto("my_user", "taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(current));
        when(userRepository.findByUsernameIgnoreCase("my_user")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete should remove user without recipes and invalidate cache")
    void deleteShouldRemoveUserWithoutRecipes() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
        verify(recipeQueryCacheService).invalidateAll();
    }

    @Test
    @DisplayName("delete should throw IllegalArgumentException when user has recipes")
    void deleteShouldThrowWhenUserHasRecipes() {
        User user = new User();
        user.setId(1L);
        user.getRecipes().add(new Recipe());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot delete user that has recipes. Remove or reassign recipes first.");

        verify(userRepository, never()).delete(any());
    }

    private UserCreateDto sampleCreateDto(String username, String email) {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setBio("Bio test");
        return dto;
    }
}
