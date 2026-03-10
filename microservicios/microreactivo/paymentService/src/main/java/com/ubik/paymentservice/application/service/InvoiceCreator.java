package com.ubik.paymentservice.application.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceCreator {

    public byte[] generateInvoice(
            String invoiceNumber,
            String customerName,
            String customerEmail,
            String customerPhone,
            String servicesDetail,
            double totalAmount
    ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            Paragraph title = new Paragraph("FACTURA DE COMPRA - UBIK")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18);
            document.add(title);

            // Fecha
            String dateFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            document.add(new Paragraph("Fecha: " + dateFormatted));
            
            // Número de Factura
            document.add(new Paragraph("Factura #: " + invoiceNumber).setBold());

            document.add(new Paragraph("\n"));

            // Datos del cliente
            document.add(new Paragraph("DATOS DEL CLIENTE").setBold());
            document.add(new Paragraph("Nombre: " + (customerName != null ? customerName : "N/A")));
            document.add(new Paragraph("Email: " + (customerEmail != null ? customerEmail : "N/A")));
            document.add(new Paragraph("Teléfono: " + (customerPhone != null ? customerPhone : "N/A")));

            document.add(new Paragraph("\n"));

            // Detalle de servicios
            document.add(new Paragraph("DETALLE DE SERVICIOS").setBold());
            document.add(new Paragraph(servicesDetail));

            document.add(new Paragraph("\n"));

            // Total
            Paragraph total = new Paragraph("TOTAL PAGADO: $" + String.format("%.2f", totalAmount) + " COP")
                    .setBold()
                    .setFontSize(14);
            document.add(total);

            // Agradecimiento
            document.add(new Paragraph("\n¡Gracias por utilizar UBIK!").setTextAlignment(TextAlignment.CENTER).setItalic());

            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generando la factura PDF", e);
        }

        return baos.toByteArray();
    }
}
