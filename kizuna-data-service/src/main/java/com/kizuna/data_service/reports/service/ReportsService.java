package com.kizuna.data_service.reports.service;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.AreaBreakType;
import com.kizuna.data_service.domain.InventoryEvent;
import com.kizuna.data_service.domain.ProductionEvent;
import com.kizuna.data_service.domain.QualityInspectionEvent;
import com.kizuna.data_service.metrics.dtos.InventoryMetricsDto;
import com.kizuna.data_service.metrics.dtos.ProductionMetricsDto;
import com.kizuna.data_service.metrics.dtos.QualityMetricsDto;
import com.kizuna.data_service.metrics.service.DashboardService;
import com.kizuna.data_service.repository.InventoryEventRepository;
import com.kizuna.data_service.repository.ProductionEventRepository;
import com.kizuna.data_service.repository.QualityInspectionEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class ReportsService {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DashboardService dashboardService;
    private final ProductionEventRepository productionRepository;
    private final InventoryEventRepository inventoryRepository;
    private final QualityInspectionEventRepository qualityRepository;

    public byte[] generatePDFReport() {
        return generatePDFReport("30d", null, null);
    }

    public byte[] generatePDFReport(String period, String from, String to) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String generatedAt = LocalDateTime.now().format(TS_FMT);

        ProductionMetricsDto production = dashboardService.getProductionMetrics(period, from, to);
        InventoryMetricsDto inventory = dashboardService.getInventoryMetrics(period, from, to);
        QualityMetricsDto quality = dashboardService.getQualityMetrics(period, from, to);

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new ReportPdfPageBackground());
            Document document = new Document(pdf);
            document.setMargins(36, 40, 40, 40);

            ReportPdfTheme.addCoverHeader(document, generatedAt);
            ReportPdfTheme.addReadingGuide(document);

            long prodEvents = productionRepository.count();
            long invEvents = inventoryRepository.count();
            long qualEvents = qualityRepository.count();

            ReportPdfTheme.addExecutiveSummary(document, new String[]{
                    "Production: " + production.totalOrders() + " orders tracked; "
                            + production.StartedOrders() + " in progress; "
                            + production.finishedOrders() + " finished.",
                    "Inventory: " + inventory.totalItems() + " item events; "
                            + inventory.lowStockItems() + " low-stock signals.",
                    "Quality: " + quality.aprovedOrders() + " approved; "
                            + quality.rejectedOrders() + " rejected inspections.",
                    "Event log size: " + prodEvents + " production, "
                            + invEvents + " inventory, " + qualEvents + " quality records."
            });

            ReportPdfTheme.addMetricRow(
                    document,
                    production.totalOrders() + " orders",
                    inventory.totalItems() + " items",
                    quality.aprovedOrders() + " approved"
            );

            ReportPdfTheme.addDataSection(
                    document,
                    "2",
                    "Production events",
                    "Order lifecycle events from the production stream (" + prodEvents + " rows).",
                    new String[]{"Order ID", "Recipe", "Status", "Timestamp"},
                    new float[]{1.2f, 2.5f, 1.5f, 2f},
                    toProductionRows()
            );

            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            ReportPdfTheme.addDataSection(
                    document,
                    "3",
                    "Inventory events",
                    "Stock movement events from the inventory stream (" + invEvents + " rows).",
                    new String[]{"Material", "Quantity", "Type", "Timestamp"},
                    new float[]{2.5f, 1f, 1.2f, 2f},
                    toInventoryRows()
            );

            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            ReportPdfTheme.addDataSection(
                    document,
                    "4",
                    "Quality inspection events",
                    "Inspection outcomes from the quality stream (" + qualEvents + " rows).",
                    new String[]{"Order ID", "Product", "Result", "Timestamp"},
                    new float[]{1.2f, 2.5f, 1.2f, 2f},
                    toQualityRows()
            );

            ReportPdfTheme.addFooter(document);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate analytics PDF", e);
        }

        return out.toByteArray();
    }

    private List<String[]> toProductionRows() {
        List<String[]> rows = new ArrayList<>();
        productionRepository.findAll().forEach(p -> rows.add(new String[]{
                formatId(p.getOrderId()),
                p.getRecipeName(),
                firstNonBlank(p.getStatus(), p.getType()),
                formatTime(p.getTimestamp())
        }));
        return rows;
    }

    private List<String[]> toInventoryRows() {
        List<String[]> rows = new ArrayList<>();
        inventoryRepository.findAll().forEach(i -> rows.add(new String[]{
                i.getInventoryName(),
                i.getQuantity() != null ? String.valueOf(i.getQuantity()) : null,
                i.getType(),
                formatTime(i.getTimestamp())
        }));
        return rows;
    }

    private List<String[]> toQualityRows() {
        List<String[]> rows = new ArrayList<>();
        qualityRepository.findAll().forEach(q -> rows.add(new String[]{
                formatId(q.getOrderId()),
                q.getProductName(),
                q.getResult(),
                formatTime(q.getTimestamp())
        }));
        return rows;
    }

    private static String formatId(Long id) {
        return id != null ? String.valueOf(id) : null;
    }

    private static String formatTime(LocalDateTime ts) {
        return ts != null ? ts.format(TS_FMT) : null;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
