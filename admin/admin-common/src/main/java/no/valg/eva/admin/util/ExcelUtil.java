package no.valg.eva.admin.util;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Excel utility class.
 */
public final class ExcelUtil {
    public static final java.util.function.Function<List<Pair<String, String>>, String[]> VALUES_FROM_PAIRS_F =
            nullableRow -> {
                List<Pair<String, String>> row = ofNullable(nullableRow).orElse(Collections.emptyList());
                return new ArrayList<>(row.stream().map(cell -> cell != null ? cell.getValue() : null).collect(Collectors.toList())).toArray(new String[row.size()]);
            };
    private static final Logger LOGGER = Logger.getLogger(ExcelUtil.class);
    private static final int MAX_NO_OF_ROWS = 10;

    private ExcelUtil() {
    }

    public static byte[] createXlsxFromRowData(final List<List<String>> rowCells) {
        return createWorkbookFromDowData(rowCells, new XSSFWorkbook());
    }

    public static byte[] createXlsFromRowData(final List<List<String>> rowCells) {
        return createWorkbookFromDowData(rowCells, new HSSFWorkbook());
    }

    private static byte[] createWorkbookFromDowData(final List<List<String>> rowCells, Workbook workbook) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = null;

        try {
            Sheet sheet = workbook.createSheet();

            buildSheet(rowCells, sheet);

            workbook.write(outputStream);
            bytes = outputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            close(workbook);
            close(outputStream);
        }

        return bytes;
    }

    private static void buildSheet(List<List<String>> rowCells, Sheet sheet) {
        int rowNumber = 0;
        for (List<String> rowList : rowCells) {
            buildRow(sheet, rowNumber, rowList);
            rowNumber++;
        }
    }

    private static void buildRow(Sheet sheet, int rowNumber, List<String> rowList) {
        int rowNum = rowList.size();
        Row row = sheet.createRow(rowNumber);
        for (int cellNum = 0; cellNum < rowNum; cellNum++) {
            buildCell(row, rowList.get(cellNum), cellNum);
        }
    }

    private static void buildCell(Row row, String value, int cellNum) {
        if (value != null) {
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(value);
            cell.getCellStyle().setLocked(false);
        }
    }

    private static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static RowData getRowDataFromExcelFile(final InputStream inStream) throws IOException, InvalidFormatException {
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inStream);
            Sheet sheet = workbook.getSheetAt(0);
            List<List<Pair<String, String>>> dataList = new ArrayList<>();

            int lastRowNum = sheet.getLastRowNum() + 1;
            int columnLength = getColumnLength(sheet, lastRowNum);
            boolean hasDescription = sheet.getNumMergedRegions() > 0;
            RowData result = new RowData(hasDescription ? 2 : 1);

            // Assume first row consists of headers only.
            // In addition, there could be a description row above that, but that should be in a cell which has been merged with others.
            // It is therefore assumed that if the worksheet contains any merged cells,
            // it must be the descritpion row, and both first and second row are to be ignored.

            // Get header values
            Row header = hasDescription ? sheet.getRow(1) : sheet.getRow(0);
            if (header != null) {
                List<Pair<String, String>> headerValues = getRowData(columnLength, header);
                if (headerValues != null) {
                    result.setHeader(headerValues);
                }
            }

            // Iterate data rows
            for (int r = result.getRowsStartIndex(); r < lastRowNum; r++) {
                Row row = sheet.getRow(r);
                List<Pair<String, String>> rowData = getRowData(columnLength, row);
                if (rowData != null) {
                    dataList.add(rowData);
                }
            }
            result.setRows(dataList);
            return result;
        } finally {
            close(workbook);
        }
    }

    private static int getColumnLength(final Sheet sheet, final int numberOfRows) {
        int maxColumns = 0;
        Row row;

        for (int i = 0; i < MAX_NO_OF_ROWS || i < numberOfRows; i++) {

            row = sheet.getRow(i);
            if (row != null) {
                int numberOfCellsOnRow = sheet.getRow(i).getPhysicalNumberOfCells();
                if (numberOfCellsOnRow > maxColumns) {
                    maxColumns = numberOfCellsOnRow;
                }
            }
        }

        return maxColumns;
    }

    private static List<Pair<String, String>> getRowData(int columnLength, Row row) {
        StringBuilder emptyRowChecker = new StringBuilder();
        List<Pair<String, String>> rowData = new ArrayList<>();
        Cell cell;
        if (row != null) {
            for (int c = 0; c < columnLength; c++) {
                cell = row.getCell(c);
                if (cell != null && getCellData(cell) == null) {
                    cell = null;
                }
                if (cell != null) {
                    String cellData = getCellData(cell);
                    rowData.add(new ImmutablePair<>(new CellReference(cell).formatAsString(), cellData));
                    emptyRowChecker.append(cellData);
                } else {
                    rowData.add(new ImmutablePair<>(new CellReference(new ExcelUtilEmptyCell(row.getRowNum(), c)).formatAsString(), null));
                }
            }
            if (emptyRowChecker.length() > 0) {
                return rowData;
            }
        }
        return Collections.emptyList();
    }

    private static String getCellData(final Cell cell) {
        String result = null;
        if (cell != null) {
            if (cell.getCellTypeEnum() == CellType.STRING) {
                result = cell.getRichStringCellValue().toString();
            } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                if (HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                    result = DateUtil.getFormattedShortDate(new DateTime(cell.getDateCellValue()));

                } else {
                    Double d = cell.getNumericCellValue();
                    result = Long.toString(d.longValue());
                }
            } else {
                result = cell.toString();
            }
        }
        if (result != null && result.trim().length() == 0) {
            result = null;
        }
        return result;
    }


    public static class RowData {
        private int rowsStartIndex;
        private List<Pair<String, String>> header;
        private List<List<Pair<String, String>>> rows;

        RowData(int rowsStartIndex) {
            this.rowsStartIndex = rowsStartIndex;
        }

        public List<Pair<String, String>> getHeader() {
            return header;
        }

        public void setHeader(List<Pair<String, String>> header) {
            this.header = header;
        }

        public List<List<Pair<String, String>>> getRows() {
            return rows;
        }

        public void setRows(List<List<Pair<String, String>>> rows) {
            this.rows = rows;
        }

        int getRowsStartIndex() {
            return rowsStartIndex;
        }
    }

    public static class ExcelUtilEmptyCell implements Cell {

        private int rowIndex;
        private int colIndex;

        ExcelUtilEmptyCell(int rowIndex, int colIndex) {
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
        }

        @Override
        public int getColumnIndex() {
            return colIndex;
        }

        @Override
        public int getRowIndex() {
            return rowIndex;
        }

        @Override
        public Sheet getSheet() {
            return null;
        }

        @Override
        public Row getRow() {
            return null;
        }

        @Override
        public void setCellType(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellType(CellType cellType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCellType() {
            return 0;
        }

        @Override
        public CellType getCellTypeEnum() {
            return null;
        }

        @Override
        public int getCachedFormulaResultType() {
            return 0;
        }

        @Override
        public CellType getCachedFormulaResultTypeEnum() {
            return null;
        }

        @Override
        public void setCellValue(double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellValue(Date value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellValue(Calendar value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellValue(RichTextString value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellValue(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellFormula(String formula) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCellFormula() {
            return null;
        }

        @Override
        public double getNumericCellValue() {
            return 0;
        }

        @Override
        public Date getDateCellValue() {
            return null;
        }

        @Override
        public RichTextString getRichStringCellValue() {
            return null;
        }

        @Override
        public String getStringCellValue() {
            return null;
        }

        @Override
        public void setCellValue(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellErrorValue(byte value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getBooleanCellValue() {
            return false;
        }

        @Override
        public byte getErrorCellValue() {
            return 0;
        }

        @Override
        public void setCellStyle(CellStyle style) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CellStyle getCellStyle() {
            return null;
        }

        @Override
        public void setAsActiveCell() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CellAddress getAddress() {
            return null;
        }

        @Override
        public void setCellComment(Comment comment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Comment getCellComment() {
            return null;
        }

        @Override
        public void removeCellComment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Hyperlink getHyperlink() {
            return null;
        }

        @Override
        public void setHyperlink(Hyperlink link) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeHyperlink() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CellRangeAddress getArrayFormulaRange() {
            return null;
        }

        @Override
        public boolean isPartOfArrayFormulaGroup() {
            return false;
        }
    }
}
