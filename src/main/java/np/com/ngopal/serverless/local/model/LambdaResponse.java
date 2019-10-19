/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package np.com.ngopal.serverless.local.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 *
 * @author ngm
 */
@Data
@ToString
@AllArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class LambdaResponse {

    private int statusCode;

    private String body;

    private Map<String, String> headers;

    private boolean isBase64Encoded;
}
