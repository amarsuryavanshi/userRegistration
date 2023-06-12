package com.UserRegistration.Controller;

import com.UserRegistration.Payload.UserDto;
import com.UserRegistration.Service.UserRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
public class UserRegistrationController {

    private UserRegistrationService userRegistrationService;

    // Twilio account credentials
    private static final String ACCOUNT_SID = "ACb46f1642ffe92fa54d7274e4840ea13f";
    private static final String AUTH_TOKEN = "e40f366a32ebcefda7ef7ec2e10a3e80";

    // Map to store generated OTPs
    private Map<String, String> otpMap = new HashMap<>();

    public UserRegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN); // Initialize Twilio client
    }

    @PostMapping("/photo/register")
    public ResponseEntity<String> registerUser(@RequestParam("photo") MultipartFile photo,
                                               @RequestParam("userDto") String userDtoJson) throws IOException {
        UserDto userDto = new ObjectMapper().readValue(userDtoJson, UserDto.class);

        if (!photo.isEmpty()) {
            userDto.setPhoto(photo);
        }

        UserDto registeredUser = userRegistrationService.registerUser(userDto);

        // Generate OTP and send SMS
        String otp = generateOTP();
        String mobile = registeredUser.getMobile();
        sendSMS(mobile, "Your OTP is: " + otp);

        // Store OTP for verification
        otpMap.put(mobile, otp);

        return ResponseEntity.ok("User registration successful");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOTP(@RequestParam("mobile") String mobile,
                                            @RequestParam("otp") String otp) {
        String storedOTP = otpMap.get(mobile);

        if (storedOTP != null && storedOTP.equals(otp)) {
            otpMap.remove(mobile);
            return ResponseEntity.ok("OTP verified successfully");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadUsers() throws IOException {
        List<UserDto> users = userRegistrationService.getAllUsers();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        createHeaderRow(sheet);
        createDataRows(users, sheet);

        workbook.write(outputStream);
        workbook.close();

        byte[] excelBytes = outputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "users.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        CellStyle headerCellStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerCellStyle.setFont(headerFont);

        String[] headers = {"ID", "First Name", "Last Name", "City", "Email", "Mobile", "State", "Country", "Pin Code"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void createDataRows(List<UserDto> users, Sheet sheet) {
        int rowNumber = 1;

        for (UserDto user : users) {
            Row row = sheet.createRow(rowNumber++);

            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getFirstName());
            row.createCell(2).setCellValue(user.getLastName());
            row.createCell(3).setCellValue(user.getCity());
            row.createCell(4).setCellValue(user.getEmail());
            row.createCell(5).setCellValue(user.getMobile());
            row.createCell(6).setCellValue(user.getState());
            row.createCell(7).setCellValue(user.getCountry());
            row.createCell(8).setCellValue(user.getPinCode());
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            Message sms = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber("+13613018784"),
                    message
            ).create();

            System.out.println("SMS Sent. Message SID: " + sms.getSid());
        } catch (ApiException e) {
            System.err.println("Error sending SMS: " + e.getMessage());
        }
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
