package no.evote.dto;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FileReaderResultsDtoTest {

	private transient FileReaderResultsDto fileRRDto;

	@Test
	public void testFieldNotReadEntries() {
		fileRRDto = new FileReaderResultsDto();
		fileRRDto.incrementNotReadEntries();
		fileRRDto.incrementNotReadEntries();
		Assert.assertEquals(fileRRDto.getNoOfNotReadEntries().intValue(), 2);
		Assert.assertEquals(fileRRDto.getNoOfReadEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.getNoOfNotStoredEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.getNoOfStoredEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.hasFailedEntries().booleanValue(), true);
	}

	@Test
	public void testFieldReadEntries() {
		fileRRDto = new FileReaderResultsDto();
		fileRRDto.incrementReadEntries();
		fileRRDto.incrementReadEntries();
		fileRRDto.incrementReadEntries();
		Assert.assertEquals(fileRRDto.getNoOfNotReadEntries().intValue(), 0);
		
		Assert.assertEquals(fileRRDto.getNoOfReadEntries().intValue(), 3);
		
		Assert.assertEquals(fileRRDto.getNoOfNotStoredEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.getNoOfStoredEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.hasFailedEntries().booleanValue(), false);
	}

	@Test
	public void testFieldNotStoredEntries() {
		fileRRDto = new FileReaderResultsDto();
		fileRRDto.incrementNotStoredEntries();
		Assert.assertEquals(fileRRDto.getNoOfNotReadEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.getNoOfReadEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.getNoOfNotStoredEntries().intValue(), 1);
		Assert.assertEquals(fileRRDto.getNoOfStoredEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.hasFailedEntries().booleanValue(), true);
	}

	@Test
	public void testFieldStoredEntries() {
		fileRRDto = new FileReaderResultsDto();
		fileRRDto.incrementStoredEntries();
		fileRRDto.incrementStoredEntries();
		Assert.assertEquals(fileRRDto.getNoOfNotReadEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.getNoOfReadEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.getNoOfNotStoredEntries().intValue(), 0);
		Assert.assertEquals(fileRRDto.getNoOfStoredEntries().intValue(), 2);
		Assert.assertEquals(fileRRDto.hasFailedEntries().booleanValue(), false);
	}

	@Test
	public void testFieldException() {
		fileRRDto = new FileReaderResultsDto();
		fileRRDto.incrementStoredEntries();
		fileRRDto.incrementNotStoredEntries();
		fileRRDto.addExceptionMessage(new StringBuilder("ABC"));
		fileRRDto.addExceptionMessage(new StringBuilder("DEF"));
		Assert.assertTrue(fileRRDto.getExceptionMessage().toString().startsWith("ABC"));
		Assert.assertEquals(fileRRDto.hasFailedEntries().booleanValue(), true);
	}

}
