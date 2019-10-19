/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package np.com.ngopal.serverless.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import np.com.ngopal.serverless.local.model.Serverless;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

/**
 * Compiles the project using the maven tool. It will watch the directory and updates the class accordingly.
 * @author ngm
 */
@Slf4j
class Compiler {

    private WatchService watcher;

    private File workDir;

    public Compiler() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void watchDir(Path dir) throws IOException {
        WatchKey key = dir.register(watcher,
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY);

    }

    public void createWorkingDir() {
        System.getProperty("java.io.tmpdir");
        File workingDir = new File(System.getProperty("java.io.tmpdir") + "/" + "aws-local-java");
        workingDir.deleteOnExit();
        workDir = workingDir;
    }

    public boolean needChanges(File f) {
        File libFile = new File(f, "target/classes/lib");
        log.debug("File Check: {}", libFile.getAbsolutePath());
        boolean check = libFile.exists();
        return !check;
    }

    public void executeDependency(File file) throws IOException {
        CommandLine line = new CommandLine("mvn");
        line.addArgument("dependency:copy-dependencies");
        line.addArgument("-DoutputDirectory=target/classes/lib");
        DefaultExecutor exector = new DefaultExecutor();
        exector.setWorkingDirectory(file);
        exector.execute(line);
    }

    public void copyProjectDir(String file, Serverless serverless) {
        try {
            File f = new File(workDir.getAbsolutePath() + "/" + serverless.getService());
            f.mkdirs();
            File sourceFile = new File(file);
            List<Path> collect = Files.walk(sourceFile.toPath()).collect(Collectors.toList());
            for(Path source : collect){
                copy(source, f.toPath().resolve(sourceFile.toPath().relativize(source)));
            }
        } catch (IOException ex) {
            Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void copy(Path source, Path dest) throws IOException {
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    }
}
