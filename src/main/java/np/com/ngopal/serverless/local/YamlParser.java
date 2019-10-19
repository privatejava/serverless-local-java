/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package np.com.ngopal.serverless.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import np.com.ngopal.serverless.local.model.Serverless;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Yaml Parser will do the parsing of yml file to Serverless Java Model
 * @author ngm
 */
public class YamlParser {

    /**
     * Parses the serverless.yml from the project directory
     * @param file String project directory
     * @return Serverless model
     */
    public static Serverless parse(String file) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Serverless serverless = mapper.readValue(new File(file + "/serverless.yml"), Serverless.class);
            System.out.println(ReflectionToStringBuilder.toString(serverless, ToStringStyle.MULTI_LINE_STYLE));
            return serverless;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
