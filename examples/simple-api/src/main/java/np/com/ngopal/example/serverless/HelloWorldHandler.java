package np.com.ngopal.example.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.sun.javafx.collections.MappingChange;

import java.util.HashMap;
import java.util.Map;

public class HelloWorldHandler {

    public Map handler(Map<String,Object> request, Context context){
        Gson gson = new Gson();
        Map body = gson.fromJson(request.get("body").toString(),Map.class);
        String name = body.containsKey("name")?body.get("name").toString():"Guest";
        Map<String,String> response= new HashMap<String, String>();
        response.put("message","Hello "+name );
        return response;
    }
}
