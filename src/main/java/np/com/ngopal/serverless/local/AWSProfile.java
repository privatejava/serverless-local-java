package np.com.ngopal.serverless.local;

import lombok.extern.slf4j.Slf4j;
import np.com.ngopal.serverless.local.model.Config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Narayan <me@ngopal.com.np> - ngm
 * Created 06/01/2020 19:05
 **/
@Slf4j
public class AWSProfile {
	public static void processAWSProfile() throws IOException {
		if (Config.PROFILE_NAME != null) {
			File f = new File(System.getProperty("user.home") + "/" + ".aws/credentials");
			if (f.exists()) {
				Map<String, Properties> stringPropertiesMap = parseINI(new FileReader(f));
				if (stringPropertiesMap.containsKey(Config.PROFILE_NAME)) {
					log.debug("Using Profile {}: {}",Config.PROFILE_NAME, stringPropertiesMap.get(Config.PROFILE_NAME));
					for (final String name : stringPropertiesMap.get(Config.PROFILE_NAME).stringPropertyNames()) {
						Config.SERVERLESS.getProvider().getEnvironment().put(name.toUpperCase(), stringPropertiesMap.get(Config.PROFILE_NAME).getProperty(name));
					}
				}

			}
		}
	}

	public static Map<String, Properties> parseINI(Reader reader) throws IOException {
		Map<String, Properties> result = new HashMap();
		new Properties() {

			private Properties section;

			@Override
			public Object put(Object key, Object value) {
				String header = (((String) key) + " " + value).trim();
				if (header.startsWith("[") && header.endsWith("]"))
					return result.put(header.substring(1, header.length() - 1),
							section = new Properties());
				else
					return section.put(key, value);
			}

		}.load(reader);
		return result;
	}

}
