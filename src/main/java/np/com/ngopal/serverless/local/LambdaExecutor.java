/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package np.com.ngopal.serverless.local;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import np.com.ngopal.serverless.local.model.Config;
import np.com.ngopal.serverless.local.model.Function;
import np.com.ngopal.serverless.local.model.LambdaResponse;
import org.apache.commons.exec.*;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * LambdaExecutor will do the actual execution of the REST API which is exposed from serverless yml file. It will use
 * the docker image lambci/lambda:java8 for executing the java class file.
 * @author ngm
 */
@Slf4j
public class LambdaExecutor {

	private static void createEnv(CommandLine line){
		Map<String, String> environment = Config.SERVERLESS.getProvider().getEnvironment();

		for(String key : environment.keySet()){
			line.addArgument("-e");
			line.addArgument(String.format("%s=%s", key,environment.get(key)));
		}

		if(Config.SERVERLESS.getProvider().getRegion()!=null){
			line.addArgument("-e");
			line.addArgument(String.format("%s=%s", "AWS_REGION",Config.SERVERLESS.getProvider().getRegion()));
		}
	}

	/**
	 * Executes the lambda function by using the classpath and the docker image then forward the container's
	 * std error and std output to the console.
	 *
	 * @param file
	 * @param function
	 * @param payload
	 * @param future
	 * @throws IOException
	 */
	public static void execute(File file, Function function, Map<String, Object> payload, CompletableFuture<Response> future) throws IOException {
		String body = UtilsFactory.get().getMapper().writeValueAsString(payload);
		CommandLine line = new CommandLine("docker");
		line.addArgument("run");
		line.addArgument("--memory");
		line.addArgument(Config.SERVERLESS.getProvider().getMemorySize() + "m");
        line.addArgument("--rm");
		createEnv(line);
		line.addArgument("-v");
		line.addArgument("" + file.getAbsolutePath() + "/target/classes:/var/task");
		line.addArgument(Config.IMAGE);
		line.addArgument(function.getHandler());
		line.addArgument(body, false);

//        line.addArgument(body);
		log.debug("{}", UtilsFactory.get().getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(payload));
		log.debug("Commandline {}", line);
		DefaultExecutor executor = new DefaultExecutor();
		ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
		ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
		PumpStreamHandler handler = new PumpStreamHandler(stdOut, stdErr);

		executor.setStreamHandler(handler);
		executor.setWorkingDirectory(file);
		executor.execute(line, new ExecuteResultHandler() {
			@Override
			public void onProcessComplete(int i) {
				log.debug("Success: {}", i);
				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(stdOut.toByteArray());
					ByteArrayInputStream baes = new ByteArrayInputStream(stdErr.toByteArray());
					InputStream allStream = null;
					if(stdErr.size() > 0){
						allStream = new SequenceInputStream(
							baes,
							bais
						);
					}else{
						allStream = bais;
					}

					Response resp = new Response(new BufferedReader(new InputStreamReader(allStream)));
					if (resp.getResponse().getBody().equals("INVALID")) {
						bais = new ByteArrayInputStream(stdErr.toByteArray());
						resp = new Response(new BufferedReader(new InputStreamReader(bais)));
					}
					future.complete(resp);
				} catch (Exception t) {
					t.printStackTrace();
					future.completeExceptionally(t);
				}

			}

			@Override
			public void onProcessFailed(ExecuteException ee) {
				log.debug("Failed");
				try {
					final ByteArrayInputStream bais = new ByteArrayInputStream(stdErr.toByteArray());
					Response resp = new Response(new BufferedReader(new InputStreamReader(bais)));
					future.complete(resp);
				} catch (Exception t) {
					future.completeExceptionally(t);
					t.printStackTrace();
				}
			}
		});

	}

	@Slf4j
	@ToString
	@Data
	@AllArgsConstructor
	static class Response {

		private String output;

		private LambdaResponse response;

		private Response(BufferedReader is) throws IOException {
			StringBuilder buffer = new StringBuilder();
			String line;
			String lastLine = null;
			while ((line = is.readLine()) != null) {
				lastLine = line;
				buffer.append(line).append("\n");
			}
			output = buffer.toString();
			log.debug("Output: {}", output);
			if (lastLine != null) {
				try {
					response = UtilsFactory.get().getMapper().readValue(lastLine, LambdaResponse.class);
				} catch (Exception exception) {
					response = new LambdaResponse(400, lastLine, Collections.emptyMap(), false);
				}
			} else {
				response = new LambdaResponse(400, "INVALID", Collections.emptyMap(), false);
			}
			System.out.println("=====================================");
			System.out.println("-------------------------------------");
			System.out.println("LAMBDA RESPONSE");
			System.out.println("-------------------------------------");
			System.out.println(UtilsFactory.get().getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
			System.out.println("-------------------------------------");
			System.out.println("LAMBDA BODY");
			System.out.println("-------------------------------------");
			try {
				Map body = UtilsFactory.get().getMapper().readValue(response.getBody(), Map.class);
				System.out.println(UtilsFactory.get().getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(body));
			} catch (Exception ex) {
				System.out.println(response.getBody());
			}


			System.out.println("=====================================");
		}
	}
}
