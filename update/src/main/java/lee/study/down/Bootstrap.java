package lee.study.down;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import lee.study.down.util.PathUtil;

public class Bootstrap {

  public static void main(String[] args) throws Exception {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (process != null) {
        process.destroy();
      }
    }));
    fork();
  }

  private static final String MAIN_PATH = PathUtil.ROOT_PATH + "main" + File.separator;
  private static final String CORE_PATH = MAIN_PATH + "proxyee-down-core.jar";
  private static final String UPDATE_PATH = MAIN_PATH + "proxyee-down-core.jar.bak";
  private static Process process;
  private static String JAVA_EXEC_HOME;

  static {
    String execPath = File.separator + "bin" + File.separator + "java.exe";
    File dir = new File(PathUtil.ROOT_PATH);
    File[] files = dir.listFiles((d, name) -> d.isDirectory() && name.indexOf("jre") > -1);
    if (files.length > 0) {
      JAVA_EXEC_HOME = files[0].getAbsolutePath()+execPath;
    } else {
      JAVA_EXEC_HOME = System.getenv("JAVA_HOME") + execPath;
    }
    JAVA_EXEC_HOME = "\"" + JAVA_EXEC_HOME + "\"";
  }

  private static void fork() throws Exception {
    process = Runtime.getRuntime().exec(JAVA_EXEC_HOME + " -jar \"" + CORE_PATH + "\"");
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    boolean isUpdate = false;
    while ((line = br.readLine()) != null) {
      System.out.println(line);
      if ("proxyee-down-update".equals(line)) {
        isUpdate = true;
        break;
      }
    }
    if (isUpdate) {
      process.destroy();
      replaceJar();
      fork();
    }
  }

  private static void replaceJar() throws InterruptedException {
    File updateFile = new File(UPDATE_PATH);
    File beforeFile = new File(CORE_PATH);
    if (updateFile.exists()) {
      if (beforeFile.exists()) {
        if (!delete(beforeFile)) {
          System.exit(1);
        }
      }
      updateFile.renameTo(new File(CORE_PATH));
    }
  }

  private static boolean delete(File file) throws InterruptedException {
    for (int i = 0; i < 30; i++) {
      if (file.delete()) {
        return true;
      }
      Thread.sleep(1000);
    }
    return false;
  }
}
