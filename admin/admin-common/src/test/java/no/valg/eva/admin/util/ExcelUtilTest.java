package no.valg.eva.admin.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;


public class ExcelUtilTest {
	@Test
	public void testCreateXlsFromRowData() throws Exception {
		List<List<String>> excelData = createExcelData();

		InputStream inputStream = new ByteArrayInputStream(ExcelUtil.createXlsFromRowData(excelData));
		ExcelUtil.RowData rowData = ExcelUtil.getRowDataFromExcelFile(inputStream);
		assertHeader(rowData, 1, 3, "A1", "A2", "A3");
		testExcelData(rowData.getRows());
	}

	@Test
	public void testCreateXlsxFromRowData() throws Exception {
		List<List<String>> excelData = createExcelData();

		InputStream inputStream = new ByteArrayInputStream(ExcelUtil.createXlsxFromRowData(excelData));
		ExcelUtil.RowData rowData = ExcelUtil.getRowDataFromExcelFile(inputStream);
		assertHeader(rowData, 1, 3, "A1", "A2", "A3");
		testExcelData(rowData.getRows());
	}

	@Test
	public void testGetRowDataFromXlsFile() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/excelReaderTestFile.xls");
		ExcelUtil.RowData rowData = ExcelUtil.getRowDataFromExcelFile(inputStream);
		inputStream.close();
		assertHeader(rowData, 1, 3, "A1", "A2", "A3");
		testExcelData(rowData.getRows());
	}

	@Test
	public void testGetRowDataFromXlsxFile() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/excelReaderTestFile.xlsx");
		ExcelUtil.RowData rowData = ExcelUtil.getRowDataFromExcelFile(inputStream);
		inputStream.close();
		assertHeader(rowData, 1, 3, "A1", "A2", "A3");
		testExcelData(rowData.getRows());
	}

	private void assertHeader(ExcelUtil.RowData rowData, int rowsStartInde, int size, String... columns) {
		assertThat(rowData.getRowsStartIndex()).isEqualTo(rowsStartInde);
		assertThat(rowData.getHeader().size()).isEqualTo(size);
		for (int i = 0; i < columns.length; i++) {
			assertThat(rowData.getHeader().get(i).getValue()).isEqualTo(columns[i]);
		}
	}

	private List<List<String>> createExcelData() {
		List<List<String>> excelData = new ArrayList<List<String>>();
		List<String> row1 = new ArrayList<String>();
		row1.add("A1");
		row1.add("A2");
		row1.add("A3");
		excelData.add(row1);
		List<String> row2 = new ArrayList<String>();
		row2.add(null);
		row2.add("2");
		row2.add("3");
		excelData.add(row2);
		List<String> row3 = new ArrayList<String>();
		row3.add("12.12.1989");
		row3.add("22222222");
		row3.add("a3");
		excelData.add(row3);
		return excelData;
	}

	private void testExcelData(final List<List<Pair<String, String>>> dataListAsPairs) {
		List<String[]> dataList = Lists.transform(dataListAsPairs, ExcelUtil.VALUES_FROM_PAIRS_F::apply);
		Assert.assertEquals(dataList.get(0)[1], "2");
		Assert.assertEquals(dataList.get(0)[2], "3");
		Assert.assertEquals(dataList.get(1)[0], "12.12.1989");
		Assert.assertEquals(dataList.get(1)[1], "22222222");
		Assert.assertEquals(dataList.get(1)[2], "a3");
		Assert.assertEquals(dataList.get(1).length, 3);
	}
}

