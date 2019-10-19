/*f
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package np.com.ngopal.serverless.local.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author ngm
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Serverless {
    private String service;
    private Provider provider;
    private Map<String,Function> functions;
    
    public Function getFunction(String functionName){
        for (String string : functions.keySet()) {
            if(functionName.equalsIgnoreCase(string)){
                return functions.get(string);
            }
        }
        throw new IllegalArgumentException("Invalid argument provided");
    }
}
