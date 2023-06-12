package com.UserRegistration.Payload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String city;
    private String email;
    private String mobile;
    private String state;
    private String country;
    private String pinCode;
    private String password;

    private MultipartFile photo;
}
