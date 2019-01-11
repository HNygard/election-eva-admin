package no.valg.eva.admin.util;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Properties;

import org.testng.annotations.Test;

public class PropertyUtilTest {
	
	@Test
	public void  addPrefixToPropertyKeys_always_addsThePrefixToAllKeys() {
		Properties properties = new Properties();
		properties.setProperty("key1", "value1");
		properties.setProperty("key2", "value2");
		properties.setProperty("key3", "value3");
		
		Properties prefixedProperties = PropertyUtil.addPrefixToPropertyKeys("prefix-", properties);
		
		assertThat(prefixedProperties.getProperty("key1")).isNull();
		assertThat(prefixedProperties.getProperty("key2")).isNull();
		assertThat(prefixedProperties.getProperty("key3")).isNull();

		assertThat(prefixedProperties.getProperty("prefix-key1")).isEqualTo("value1");
		assertThat(prefixedProperties.getProperty("prefix-key2")).isEqualTo("value2");
		assertThat(prefixedProperties.getProperty("prefix-key3")).isEqualTo("value3");
	}

}