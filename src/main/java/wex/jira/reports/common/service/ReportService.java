package wex.jira.reports.common.service;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
import wex.jira.reports.common.model.JiraResponseData;
import wex.jira.reports.common.model.report.Report;
import wex.jira.reports.common.model.report.ReportItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
public class ReportService {

    public void createReport(JiraResponseData data, String project, String outputPath) {
        Report reportData = new Report();
        data.getIssues().forEach(issue -> {
            LocalDate date = issue.getFields().getResolutionDate() != null ? issue.getFields().getResolutionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : issue.getFields().getUpdated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            Integer year = date.getYear();
            Integer weekNumber = date.get(weekFields.weekOfWeekBasedYear());

            reportData.getItems().compute(year, (k, v) -> {
                if (v == null) {
                    v = new HashSet<>();
                }
                if (v.stream().noneMatch(map -> map.containsKey(weekNumber))) {
                    HashMap<Integer, Set<ReportItem>> newValue = new HashMap<>();
                    newValue.put(weekNumber, new HashSet<>());
                    v.add(newValue);
                    return v;
                }
                return v;
            });

            reportData.getItems().get(year).stream().filter(map -> map.containsKey(weekNumber)).findFirst().orElse(new HashMap<>()).computeIfPresent(weekNumber, (k, v) -> {
                if (v.isEmpty()) {
                    ReportItem reportItem = new ReportItem();
                    reportItem.setAssignee(issue.getFields().getAssignee().getName());
                    reportItem.setNumOfTasks(1);
                    reportItem.getIssueIds().add(issue.getId());
                    v.add(reportItem);
                    return v;
                }

                ReportItem reportItem = v.stream().filter(item -> item.getAssignee().equals(issue.getFields().getAssignee().getName())).peek(item -> {
                    item.addNumOfTasks();
                    item.getIssueIds().add(issue.getId());
                }).findFirst().orElseGet(() -> {
                    ReportItem newItem = new ReportItem();
                    newItem.setAssignee(issue.getFields().getAssignee().getName());
                    newItem.setNumOfTasks(1);
                    newItem.getIssueIds().add(issue.getId());
                    return newItem;
                });

                v.add(reportItem);
                return v;
            });
        });

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(project + " - Jira Report");
        List<String> headerTitles = Arrays.asList("Year", "Week of the Year", "From", "To", "Assignee", "Number of Tasks", "Task Ids");

        for (int i = 0; i < headerTitles.size(); i++) {
            Row row = sheet.getRow(0);
            if (row == null) {
                row = sheet.createRow(0);
            }
            Cell cell = row.createCell(i);
            cell.setCellValue(headerTitles.get(i));
        }

        int yearRowStartIndex = 1;

        int weekOfTheYearStartIndex = 1;
        int weekOfTheYearEndIndex = weekOfTheYearStartIndex;

        CellStyle centerCellStyle = workbook.createCellStyle();
        centerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        centerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        for (Integer year : reportData.getItems().keySet()) {
            Set<HashMap<Integer, Set<ReportItem>>> mapSet = reportData.getItems().get(year);
            Row yearRow = sheet.createRow(yearRowStartIndex);
            Cell yearCell = yearRow.createCell(0);


            yearCell.setCellValue(year);
            yearCell.setCellStyle(centerCellStyle);

            for (HashMap<Integer, Set<ReportItem>> weekOfYearMap : mapSet) {
                for (Integer weekNumber : weekOfYearMap.keySet()) {
                    Set<ReportItem> items = weekOfYearMap.get(weekNumber);
                    String weekOfYear = "W" + String.format("%02d", weekNumber);

                    Row weekOfYearRow = sheet.getRow(weekOfTheYearStartIndex);
                    if (weekOfYearRow == null)
                        weekOfYearRow = sheet.createRow(weekOfTheYearStartIndex);

                    Cell weekOfYearCell = weekOfYearRow.createCell(1);

                    weekOfYearCell.setCellValue(weekOfYear);
                    weekOfYearCell.setCellStyle(centerCellStyle);

                    WeekFields weekFields = WeekFields.of(Locale.getDefault());
                    LocalDate date = LocalDate.now()
                            .withYear(year)
                            .with(weekFields.weekOfYear(), weekNumber)
                            .with(weekFields.dayOfWeek(), 1);

                    DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
                    String startOfWeek = date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).toString();

                    DayOfWeek lastDayOfWeek = firstDayOfWeek.plus(6);
                    String endOfWeek = date.with(TemporalAdjusters.nextOrSame(lastDayOfWeek)).toString();

                    Cell startOfWeekCell = weekOfYearRow.createCell(2);
                    startOfWeekCell.setCellValue(startOfWeek);
                    startOfWeekCell.setCellStyle(centerCellStyle);

                    Cell endOfWeekCell = weekOfYearRow.createCell(3);
                    endOfWeekCell.setCellValue(endOfWeek);
                    endOfWeekCell.setCellStyle(centerCellStyle);

                    int itemRowIndex = weekOfTheYearStartIndex;
                    for (ReportItem item : items) {
                        Row itemRow = sheet.getRow(itemRowIndex);
                        if (itemRow == null)
                            itemRow = sheet.createRow(itemRowIndex);
                        Cell assigneeCell = itemRow.createCell(4);
                        Cell numOfTasksCell = itemRow.createCell(5);
                        Cell taskIds = itemRow.createCell(6);

                        assigneeCell.setCellValue(item.getAssignee());
                        numOfTasksCell.setCellValue(item.getNumOfTasks());
                        taskIds.setCellValue(String.join(", ", item.getIssueIds()));
                        itemRowIndex++;
                    }

                    if (itemRowIndex - weekOfTheYearStartIndex > 1) {
                        sheet.addMergedRegion(new CellRangeAddress(weekOfTheYearStartIndex, itemRowIndex - 1, 1, 1));
                        sheet.addMergedRegion(new CellRangeAddress(weekOfTheYearStartIndex, itemRowIndex - 1, 2, 2));
                        sheet.addMergedRegion(new CellRangeAddress(weekOfTheYearStartIndex, itemRowIndex - 1, 3, 3));
                    }

                    weekOfTheYearStartIndex = itemRowIndex;
                    weekOfTheYearEndIndex = weekOfTheYearStartIndex;
                }
            }
        }

        if (weekOfTheYearEndIndex - yearRowStartIndex > 1) {
            sheet.addMergedRegion(new CellRangeAddress(yearRowStartIndex, weekOfTheYearEndIndex - 1, 0, 0));
        }

        try {
            FileOutputStream out = new FileOutputStream(new File(outputPath + "\\" + project + " - Jira Report.xls"));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
