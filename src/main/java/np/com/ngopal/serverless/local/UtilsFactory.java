/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package np.com.ngopal.serverless.local;


import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This clas is used for doing the utility stuffs which might be useful to run the application.
 * @author ngm
 */
@Slf4j
public class UtilsFactory {

    private static UtilsFactory instance;


    @Getter
    private ObjectMapper mapper;
    
    @Getter
    private ObjectMapper noQuoteMapper;

    private UtilsFactory() {
        noQuoteMapper = new ObjectMapper();
        noQuoteMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        noQuoteMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        noQuoteMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false); 
        noQuoteMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        noQuoteMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        
        mapper = new ObjectMapper();
        
//        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

    public static UtilsFactory get() {
        if (instance == null) {
            instance = new UtilsFactory();
        }
        return instance;
    }


}
