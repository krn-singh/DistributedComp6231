package logger;

import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;

public class LogManager {
	public Logger mLogger;
	public FileHandler fileManager;
	
	public LogManager(String name) {
		try {
			mLogger = Logger.getLogger(LogManager.class.getName());
			mLogger.setUseParentHandlers(false);
			fileManager = new FileHandler(name+".log", true);
			SimpleFormatter format = new SimpleFormatter();
			fileManager.setFormatter(format);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
