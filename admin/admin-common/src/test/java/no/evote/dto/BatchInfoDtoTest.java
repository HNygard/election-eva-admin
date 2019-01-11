package no.evote.dto;

import org.joda.time.DateTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class BatchInfoDtoTest {

    private DateTime timestamp = new DateTime();
    private String infoText = "infoText";
    private Long pk = 1L;
    private Integer batchStatus = 1;

    @Test
    public void testToString() {
        BatchInfoDto batchInfoDto = new BatchInfoDto(pk, timestamp, batchStatus, infoText);
        String result = String.format("<%d, %s, %d, %s>", pk, timestamp, batchStatus, infoText);
        assertEquals(batchInfoDto.toString(), result);
    }

    @Test
    public void testGetters() {
        BatchInfoDto batchInfoDto = new BatchInfoDto(pk, timestamp, batchStatus, infoText);
        assertEquals(batchInfoDto.getPk(), pk);
        assertEquals(batchInfoDto.getStatus(), batchStatus);
        assertEquals(batchInfoDto.getInfoText(), infoText);
        assertEquals(batchInfoDto.getTimestamp(), timestamp);
    }

    @Test(dataProvider = "batchInfoDataProvider")
    public void testEquals_GivenBatchInfoDto_VerifiesEquals(BatchInfoDto batchInfoDto, BatchInfoDto expectedBatchInfoDto) {
        assertEquals(batchInfoDto, expectedBatchInfoDto);
    }

    @Test(dataProvider = "batchInfoDataProvider")
    public void testSetters_GivenBatchInfoDto_VerifiesGetters(BatchInfoDto batchInfoDto, BatchInfoDto expectedBatchInfoDto) {
        assertEquals(batchInfoDto.getTimestamp(), expectedBatchInfoDto.getTimestamp());
        assertEquals(batchInfoDto.getInfoText(), expectedBatchInfoDto.getInfoText());
        assertEquals(batchInfoDto.getStatus(), expectedBatchInfoDto.getStatus());
        assertEquals(batchInfoDto.getPk(), expectedBatchInfoDto.getPk());
    }

    @DataProvider
    public Object[][] batchInfoDataProvider() {
        String editedInfoText = "anotherInfoText";
        Long editedPk = 2L;
        Integer editedStatus = 2;
        DateTime editedTime = new DateTime();

        BatchInfoDto batchInfoDto = new BatchInfoDto(pk, timestamp, batchStatus, infoText);
        batchInfoDto.setInfoText(editedInfoText);
        batchInfoDto.setPk(editedPk);
        batchInfoDto.setStatus(editedStatus);
        batchInfoDto.setTimestamp(editedTime);

        BatchInfoDto expectedBatchInfoDto = new BatchInfoDto(editedPk, editedTime, editedStatus, editedInfoText);

        return new Object[][]{
                {batchInfoDto, expectedBatchInfoDto}
        };
    }
}