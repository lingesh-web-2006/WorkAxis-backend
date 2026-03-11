package com.payroll.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.payroll.entity.Payroll;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class PayslipService {

    private final Locale indiaLocale = new Locale("en", "IN");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(indiaLocale);

    public void exportPayslipToPDF(Payroll payroll, HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Fonts
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
        Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

        // Header
        Paragraph title = new Paragraph("PAYROLL PRO — SALARY STATEMENT", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Professional Enterprise Payroll Management System", fontSubtitle);
        subtitle.setAlignment(Paragraph.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Employee Info Table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);

        addCell(infoTable, "Employee Name:", fontBold);
        addCell(infoTable, payroll.getEmployee().getName(), fontNormal);
        addCell(infoTable, "Employee ID:", fontBold);
        addCell(infoTable, String.valueOf(payroll.getEmployee().getId()), fontNormal);
        addCell(infoTable, "Department:", fontBold);
        addCell(infoTable, payroll.getEmployee().getDepartment(), fontNormal);
        addCell(infoTable, "Month/Year:", fontBold);
        addCell(infoTable, MonthName(payroll.getPayMonth()) + " " + payroll.getPayYear(), fontNormal);

        document.add(infoTable);

        // Salary Components Table
        PdfPTable salaryTable = new PdfPTable(2);
        salaryTable.setWidthPercentage(100);
        salaryTable.setSpacingBefore(20);
        salaryTable.setSpacingAfter(20);

        // Earnings Header
        PdfPCell earningsHeader = new PdfPCell(new Phrase("EARNINGS", fontBold));
        earningsHeader.setBackgroundColor(new Color(240, 240, 240));
        earningsHeader.setColspan(2);
        earningsHeader.setPadding(8);
        salaryTable.addCell(earningsHeader);

        addSalaryRow(salaryTable, "Basic Salary", payroll.getBasicSalary(), fontNormal);
        addSalaryRow(salaryTable, "HRA (40%)", payroll.getHra(), fontNormal);
        addSalaryRow(salaryTable, "Bonus", payroll.getBonus(), fontNormal);
        addSalaryRow(salaryTable, "Other Allowances", payroll.getOtherAllowances(), fontNormal);

        // Deductions Header
        PdfPCell deductionsHeader = new PdfPCell(new Phrase("DEDUCTIONS", fontBold));
        deductionsHeader.setBackgroundColor(new Color(240, 240, 240));
        deductionsHeader.setColspan(2);
        deductionsHeader.setPadding(8);
        salaryTable.addCell(deductionsHeader);

        addSalaryRow(salaryTable, "PF (12%)", payroll.getPfDeduction(), fontNormal);
        addSalaryRow(salaryTable, "Income Tax", payroll.getTaxDeduction(), fontNormal);
        addSalaryRow(salaryTable, "Other Deductions", payroll.getOtherDeductions(), fontNormal);

        // Summary
        PdfPCell netSalaryCell = new PdfPCell(new Phrase("NET SALARY", fontBold));
        netSalaryCell.setPadding(10);
        salaryTable.addCell(netSalaryCell);

        PdfPCell netSalaryValue = new PdfPCell(new Phrase(currencyFormatter.format(payroll.getNetSalary()), fontBold));
        netSalaryValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        netSalaryValue.setPadding(10);
        salaryTable.addCell(netSalaryValue);

        document.add(salaryTable);

        // Footer
        Paragraph footer = new Paragraph("This is a computer-generated document and does not require a signature.", fontSubtitle);
        footer.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    public void exportPayrollListToExcel(List<Payroll> payrolls, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Monthly Payroll");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] columns = {"ID", "Employee", "Department", "Month", "Year", "Basic", "Allowances", "Deductions", "Net Salary", "Status"};
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Payroll p : payrolls) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getEmployee().getName());
            row.createCell(2).setCellValue(p.getEmployee().getDepartment());
            row.createCell(3).setCellValue(MonthName(p.getPayMonth()));
            row.createCell(4).setCellValue(p.getPayYear());
            row.createCell(5).setCellValue(p.getBasicSalary().doubleValue());
            row.createCell(6).setCellValue(p.getTotalAllowances().doubleValue());
            row.createCell(7).setCellValue(p.getTotalDeductions().doubleValue());
            row.createCell(8).setCellValue(p.getNetSalary().doubleValue());
            row.createCell(9).setCellValue(p.getStatus().name());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addSalaryRow(PdfPTable table, String label, BigDecimal value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(currencyFormatter.format(value), font));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private String MonthName(int month) {
        return java.time.Month.of(month).name();
    }
}
