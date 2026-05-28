package com.kizuna.data_service.reports.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

/**
 * KIZUNA industrial PDF palette — readable for humans and AI document parsers.
 */
final class ReportPdfTheme {

    static final DeviceRgb RED = new DeviceRgb(220, 38, 38);
    static final DeviceRgb BG_DARK = new DeviceRgb(12, 12, 18);
    static final DeviceRgb SURFACE = new DeviceRgb(22, 22, 30);
    static final DeviceRgb ROW_ALT = new DeviceRgb(30, 30, 40);
    /** Primary body text on dark backgrounds */
    static final DeviceRgb TEXT = new DeviceRgb(241, 245, 249);
    /** Secondary labels — still readable on dark */
    static final DeviceRgb MUTED = new DeviceRgb(203, 213, 225);
    static final DeviceRgb EMERALD = new DeviceRgb(52, 211, 153);
    static final DeviceRgb AMBER = new DeviceRgb(251, 191, 36);

    private ReportPdfTheme() {}

    static void addCoverHeader(Document doc, String generatedAt) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(8);

        Cell brand = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(BG_DARK)
                .setPadding(16)
                .add(new Paragraph("KIZUNA")
                        .setBold()
                        .setFontSize(28)
                        .setFontColor(RED)
                        .setMarginBottom(0))
                .add(new Paragraph("INDUSTRIAL MANAGEMENT SYSTEM")
                        .setFontSize(8)
                        .setCharacterSpacing(1.5f)
                        .setFontColor(MUTED));

        Cell meta = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(BG_DARK)
                .setPadding(16)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(metaLine("Document", "Operational Analytics Report"))
                .add(metaLine("Source", "kizuna-data-service"))
                .add(metaLine("Generated", generatedAt))
                .add(new Paragraph("Status: NOMINAL")
                        .setFontSize(9)
                        .setFontColor(EMERALD)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMarginTop(4));

        header.addCell(brand);
        header.addCell(meta);
        doc.add(header);
        doc.add(new LineSeparator(new SolidLine(2f)).setStrokeColor(RED).setMarginBottom(14));
    }

    static void addReadingGuide(Document doc) {
        doc.add(sectionLabel("Reading guide"));
        doc.add(box(
                "This PDF is structured for quick scanning and AI analysis. "
                        + "Section 1 summarizes dashboard metrics from the data service. "
                        + "Sections 2–4 list event streams (production, inventory, quality) in tables. "
                        + "Each row is one recorded event with timestamp.",
                SURFACE
        ));
    }

    static void addExecutiveSummary(Document doc, String[] bullets) {
        doc.add(sectionLabel("1 — Executive summary"));
        StringBuilder body = new StringBuilder();
        for (String bullet : bullets) {
            body.append("• ").append(bullet).append("\n\n");
        }
        doc.add(box(body.toString().trim(), SURFACE));
        doc.add(spacer(10));
    }

    static void addMetricRow(Document doc, String production, String inventory, String quality) {
        Table row = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(16);
        row.addCell(metricCard("Production", production));
        row.addCell(metricCard("Inventory", inventory));
        row.addCell(metricCard("Quality", quality));
        doc.add(row);
    }

    static void addDataSection(
            Document doc,
            String sectionNumber,
            String title,
            String subtitle,
            String[] headers,
            float[] widths,
            Iterable<String[]> rows
    ) {
        doc.add(sectionLabel(sectionNumber + " — " + title));
        doc.add(new Paragraph(subtitle)
                .setFontSize(10)
                .setFontColor(TEXT)
                .setMarginBottom(8));

        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
        for (String h : headers) {
            table.addHeaderCell(headerCell(h));
        }
        int i = 0;
        for (String[] row : rows) {
            boolean alt = i % 2 == 1;
            for (String cell : row) {
                table.addCell(dataCell(safe(cell), alt));
            }
            i++;
        }
        if (i == 0) {
            table.addCell(new Cell(1, headers.length)
                    .add(new Paragraph("No events recorded in this stream.")
                            .setFontSize(9)
                            .setFontColor(MUTED))
                    .setBackgroundColor(SURFACE)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(12));
        }
        doc.add(table);
        doc.add(spacer(14));
    }

    static void addFooter(Document doc) {
        doc.add(new LineSeparator(new SolidLine(0.5f)).setStrokeColor(MUTED).setMarginTop(8));
        doc.add(new Paragraph("KIZUNA Data Service · Event-sourced analytics · Confidential")
                .setFontSize(7)
                .setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(6));
    }

    private static Paragraph metaLine(String label, String value) {
        return new Paragraph(label + ": " + value)
                .setFontSize(8)
                .setFontColor(MUTED)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2);
    }

    private static Paragraph sectionLabel(String text) {
        return new Paragraph(text.toUpperCase())
                .setBold()
                .setFontSize(11)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(RED)
                .setPadding(6)
                .setMarginTop(6)
                .setMarginBottom(10);
    }

    private static Paragraph box(String text, DeviceRgb bg) {
        return new Paragraph(text)
                .setFontSize(10)
                .setFontColor(TEXT)
                .setBackgroundColor(bg)
                .setPadding(14)
                .setBorder(new SolidBorder(RED, 0.8f))
                .setMarginBottom(12);
    }

    private static Cell metricCard(String label, String value) {
        return new Cell()
                .setBackgroundColor(SURFACE)
                .setBorder(new SolidBorder(RED, 0.8f))
                .setPadding(12)
                .add(new Paragraph(label)
                        .setFontSize(8)
                        .setFontColor(RED)
                        .setBold()
                        .setCharacterSpacing(0.8f))
                .add(new Paragraph(value)
                        .setFontSize(16)
                        .setBold()
                        .setFontColor(TEXT)
                        .setMarginTop(4));
    }

    private static Cell headerCell(String text) {
        return new Cell()
                .add(new Paragraph(text)
                        .setBold()
                        .setFontSize(8)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(RED)
                .setBorder(Border.NO_BORDER)
                .setPadding(8);
    }

    private static Cell dataCell(String text, boolean alt) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(9).setFontColor(TEXT))
                .setBackgroundColor(alt ? ROW_ALT : SURFACE)
                .setBorder(new SolidBorder(new DeviceRgb(55, 55, 72), 0.5f))
                .setPadding(8);
    }

    private static Paragraph spacer(float pt) {
        return new Paragraph(" ").setMarginBottom(pt);
    }

    private static String safe(String v) {
        return v == null || v.isBlank() ? "—" : v.trim();
    }
}
