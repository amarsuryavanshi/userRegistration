package com.UserRegistration.Service.Impl;

import com.UserRegistration.Entity.User;
import com.UserRegistration.Payload.UserDto;
import com.UserRegistration.Repository.UserRepository;
import com.UserRegistration.Service.UserRegistrationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private UserRepository userRepository;
    private ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;

    @Value("${user.photo.upload-dir}")
    private String uploadDir;

    public UserRegistrationServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto registerUser(UserDto userDto) throws IOException {
        User user = modelMapper.map(userDto, User.class);

        if (userDto.getPhoto() != null && !userDto.getPhoto().isEmpty()) {
            String photoPath = uploadUserPhoto(userDto.getPhoto());
            user.setUserPhoto(photoPath);
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public String uploadUserPhoto(MultipartFile photo) throws IOException {
        if (photo.isEmpty()) {
            throw new IllegalArgumentException("Photo is required");
        }

        String fileName = StringUtils.cleanPath(photo.getOriginalFilename());
        String fileExtension = getFileExtension(fileName);

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = File.createTempFile("photo_", fileExtension, directory);
        photo.transferTo(file);

        return file.getAbsolutePath();
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());

    }
}