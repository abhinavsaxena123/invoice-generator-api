package com.project.invoiceGeneratorApi.controller;

import com.project.invoiceGeneratorApi.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("api/emails")
@RequiredArgsConstructor
//@CrossOrigin("*")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/sendInvoice")
    public ResponseEntity<String> sendInvoice(@RequestPart("file") MultipartFile file,
                                              @RequestPart("email") String customerEmail) {

        // Validation: Check if the file and email are present before proceeding.
        if (file.isEmpty() || customerEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("File or email cannot be empty.");
        }

        try {
            emailService.sendInvoiceEmail(customerEmail, file);
            return ResponseEntity.ok("Invoice Sent Successfully.");
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send invoice.");
        }
    }



}
