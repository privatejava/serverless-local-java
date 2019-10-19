/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package np.com.ngopal.serverless.local;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import np.com.ngopal.serverless.local.LambdaExecutor.Response;
import np.com.ngopal.serverless.local.model.Config;
import np.com.ngopal.serverless.local.model.Event;
import np.com.ngopal.serverless.local.model.Function;
import np.com.ngopal.serverless.local.model.Serverless;
import spark.Request;
import spark.Route;

import static spark.Spark.*;

/**
 * @author ngm
 */
@AllArgsConstructor
@Slf4j
public class ApiServer {

	private File directory;

	private Serverless serverless;

	private final Compiler compiler = new Compiler();

	private Map<String, Object> convert(Request request) {
		Map<String, Object> data = new HashMap<>();
		data.put("body", request.body());
		Map<String, String> headers = new HashMap<>();
		Map<String, String> pathParameters = new HashMap<>();
		for (String header : request.headers()) {
			headers.put(header, request.headers(header));
		}
		for (Map.Entry<String, String> entry : request.params().entrySet()) {
			pathParameters.put(entry.getKey().substring(1), entry.getValue());
		}
		data.put("headers", headers);
		data.put("pathParameters", pathParameters);
		data.put("httpMethod", request.requestMethod());
		data.put("path", request.uri());
		return data;
	}

	private Route getRoute(Function function){

		return (request, response) -> {
			if (compiler.needChanges(Config.WORKING_DIR_FILE)) {
				compiler.executeDependency(Config.WORKING_DIR_FILE);
			}
			CompletableFuture<Response> completableFuture = new CompletableFuture<>();
			LambdaExecutor.execute(directory, function, convert(request), completableFuture);
			response.type("application/json");
			response.body(completableFuture.get().getResponse().getBody());
			response.status(completableFuture.get().getResponse().getStatusCode());
			for (Map.Entry<String, String> entry1 : completableFuture.get().getResponse().getHeaders().entrySet()) {
				response.header(entry1.getKey(), entry1.getValue());
			}
			return response.body();
		};
	}

	/**
	 * Starts a new server listening to the port.
	 */
	public void start() {
		port(Config.PORT);
		for (Map.Entry<String, Function> entry : serverless.getFunctions().entrySet()) {
			if (entry.getValue() != null && entry.getValue().getEvents() != null) {
				for (Event e : entry.getValue().getEvents()) {
					String path = e.getHttp().getPath().replace("{", ":").replace("}", "");
					if (e.getHttp().getMethod().equalsIgnoreCase("get")) {
						get(path, getRoute(entry.getValue()));
						log.debug("Mapping [{}]\t-> {}", "GET", path);
					} else if (e.getHttp().getMethod().equalsIgnoreCase("post")){
						post(path, getRoute(entry.getValue()));
						log.debug("Mapping [{}]\t-> {}", "POST", path);
					}else if (e.getHttp().getMethod().equalsIgnoreCase("patch")){
						patch(path, getRoute(entry.getValue()));
						log.debug("Mapping [{}]\t-> {}", "PATCH", path);
					}else if (e.getHttp().getMethod().equalsIgnoreCase("delete")){
						delete(path, getRoute(entry.getValue()));
						log.debug("Mapping [{}]\t-> {}", "DELETE", path);
					}
				}
			}

		}

	}
}
