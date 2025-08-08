package com.project.invoiceGeneratorApi.service;

import com.project.invoiceGeneratorApi.entity.Invoice;
import com.project.invoiceGeneratorApi.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public Invoice saveInvoice(Invoice invoice) {
        System.out.println("Saving invoice: " + invoice);
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> fetchInvoices(String clerkId) {
        try {
            List<Invoice> invoices = invoiceRepository.findByClerkId(clerkId);
            return invoices != null ? invoices : new ArrayList<>(); // Ensure you return an empty list, not null.
        } catch (Exception e) {
            log.error("Error fetching invoices for clerkId {}: {}", clerkId, e.getMessage());
            return new ArrayList<>(); // Return an empty list on error to prevent frontend issues.
        }
    }

    public void removeInvoice(String invoiceId, String clerkId) {
        // 1. Attempt to find the invoice by its ID.
        Invoice existingInvoice = invoiceRepository.findByClerkIdAndId(invoiceId, clerkId)
                                        .orElseThrow(() -> new RuntimeException("Invoice Not Found: " + invoiceId));

        // 2. If the invoice was found, delete it using the repository.
        invoiceRepository.delete(existingInvoice);
    }

}
