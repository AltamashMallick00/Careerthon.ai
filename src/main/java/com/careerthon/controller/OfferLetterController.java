package com.careerthon.controller;

import com.careerthon.service.OfferLetterService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/offer-letter")
public class OfferLetterController {

    private final OfferLetterService offerLetterService;

    public OfferLetterController(OfferLetterService offerLetterService) {
        this.offerLetterService = offerLetterService;
    }

    @GetMapping
    public String showOfferLetterGenerator() {
        return "admin/offer_letter";
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateOfferLetter(
            @RequestParam String candidateName,
            @RequestParam String role,
            @RequestParam String startDate,
            @RequestParam String stipend,
            @RequestParam String issueDate) {

        byte[] pdfBytes = offerLetterService.generateOfferLetter(candidateName, role, startDate, stipend, issueDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "Offer_Letter_" + candidateName.replaceAll("\\s+", "_") + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
