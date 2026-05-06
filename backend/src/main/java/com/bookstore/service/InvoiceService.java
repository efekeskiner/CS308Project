package com.bookstore.service;

import com.bookstore.dto.InvoiceDto;
import com.bookstore.model.Invoice;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.InvoiceRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Value("${app.invoice.storage-path:./invoices}")
    private String storagePath;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Invoice createInvoice(Order order, User user) {
        File dir = new File(storagePath);
        try {
            Files.createDirectories(dir.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create invoice storage directory: " + dir, e);
        }

        String fileName = "invoice-" + order.getId() + ".pdf";
        String filePath = storagePath + File.separator + fileName;

        generatePdf(order, user, filePath);

        Invoice invoice = new Invoice(order, filePath);
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoice(Long invoiceId, User requestingUser) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceId));

        if (requestingUser.getRole() == Role.SALES_MANAGER || requestingUser.getRole() == Role.PRODUCT_MANAGER) {
            return invoice;
        }
        Long ownerId = invoice.getOrder().getUser() != null ? invoice.getOrder().getUser().getId() : null;
        if (!requestingUser.getId().equals(ownerId)) {
            throw new SecurityException("Access denied");
        }
        return invoice;
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> listInvoices(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        java.time.LocalDateTime start = (startDate != null && !startDate.isBlank())
                ? java.time.LocalDate.parse(startDate).atStartOfDay()
                : java.time.LocalDateTime.of(2000, 1, 1, 0, 0);
        java.time.LocalDateTime end = (endDate != null && !endDate.isBlank())
                ? java.time.LocalDate.parse(endDate).atTime(23, 59, 59)
                : java.time.LocalDateTime.now();
        return invoiceRepository.findByCreatedAtBetween(start, end)
                .stream()
                .map(InvoiceDto::new)
                .collect(Collectors.toList());
    }

    private void generatePdf(Order order, User user, String filePath) {
        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(44, 44, 44));
            Font sectionFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(44, 44, 44));
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(60, 60, 60));
            Font smallFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(100, 100, 100));

            // Header
            Paragraph title = new Paragraph("Online Bookstore", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph subtitle = new Paragraph("INVOICE", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(100, 60, 30)));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(4);
            doc.add(subtitle);

            doc.add(new Paragraph(" "));

            // Invoice meta
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().format(dtf) : "N/A";

            doc.add(new Paragraph("Invoice #: " + order.getId(), sectionFont));
            doc.add(new Paragraph("Order #: " + order.getId(), normalFont));
            doc.add(new Paragraph("Date: " + dateStr, normalFont));
            doc.add(new Paragraph(" "));

            // Customer info
            doc.add(new Paragraph("Bill To:", sectionFont));
            doc.add(new Paragraph("Name: " + user.getName(), normalFont));
            doc.add(new Paragraph("Email: " + user.getEmail(), normalFont));
            if (order.getDeliveryAddress() != null) {
                doc.add(new Paragraph("Delivery Address: " + order.getDeliveryAddress(), normalFont));
            }
            doc.add(new Paragraph(" "));

            doc.add(buildItemsTable(order, normalFont));
            doc.add(new Paragraph(" "));

            // Footer
            Paragraph footer = new Paragraph("Thank you for your purchase!", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    private PdfPTable buildItemsTable(Order order, Font normalFont) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4f, 1f, 2f, 2f});

        Color headerBg = new Color(75, 46, 46);
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        for (String col : new String[]{"Product", "Qty", "Unit Price", "Total"}) {
            PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setPadding(6);
            table.addCell(cell);
        }

        Color rowAlt = new Color(248, 244, 238);
        int rowIndex = 0;
        for (OrderItem item : order.getItems()) {
            Color bg = (rowIndex++ % 2 == 0) ? Color.WHITE : rowAlt;
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            PdfPCell nameCell = new PdfPCell(new Phrase(item.getProduct().getName(), normalFont));
            nameCell.setBackgroundColor(bg);
            nameCell.setPadding(5);
            table.addCell(nameCell);

            PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
            qtyCell.setBackgroundColor(bg);
            qtyCell.setPadding(5);
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(qtyCell);

            PdfPCell priceCell = new PdfPCell(new Phrase("₺" + item.getUnitPrice().toPlainString(), normalFont));
            priceCell.setBackgroundColor(bg);
            priceCell.setPadding(5);
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(priceCell);

            PdfPCell totalCell = new PdfPCell(new Phrase("₺" + lineTotal.toPlainString(), normalFont));
            totalCell.setBackgroundColor(bg);
            totalCell.setPadding(5);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
        }

        Font totalFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(44, 44, 44));
        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setColspan(2);
        table.addCell(emptyCell);

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", totalFont));
        totalLabel.setPadding(6);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase("₺" + order.getTotalPrice().toPlainString(), totalFont));
        totalValue.setPadding(6);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalValue);

        return table;
    }
}
