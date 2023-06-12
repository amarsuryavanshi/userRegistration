package com.UserRegistration.Service;

import com.UserRegistration.Payload.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserRegistrationService {
    UserDto registerUser(UserDto userDto) throws IOException;

    List<UserDto> getAllUsers();
    String uploadUserPhoto(MultipartFile photo) throws IOException;
}
