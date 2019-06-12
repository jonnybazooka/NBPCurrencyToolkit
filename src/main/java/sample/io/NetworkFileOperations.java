package sample.io;

import com.google.gson.Gson;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sample.neuralNetwork.Network;

import java.io.*;


public class NetworkFileOperations {
    private static final Logger LOGGER = LogManager.getLogger(NetworkFileOperations.class.getName());

    private final String dirPath = ".\\savedNetworks";
    private String filePath;

    public NetworkFileOperations(TextField networkNameField) {
        this.filePath = ".\\savedNetworks\\" + networkNameField.getText() + ".txt";
    }

    public boolean saveToFile(Network network) {
        LOGGER.debug("Attempting save to file operation. File path: " + filePath);
        Gson gson = new Gson();
        String savedJson = gson.toJson(network);

        File file = new File(filePath);
        File dir = new File(dirPath);
        try {
            dir.mkdirs();
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter streamWriter = new OutputStreamWriter(fOut);
            streamWriter.write(savedJson);
            streamWriter.close();
            fOut.close();
            return true;
        } catch (IOException e) {
            LOGGER.error("Cannot create new file. Path: " + filePath);
            return false;
        }
    }

    public Network loadFromFile() {
        LOGGER.debug("Attempting load from file operation. File path: " + filePath);
        File file = new File(filePath);
        try {
            FileInputStream fIn = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fIn);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder sB = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sB.append(line);
            }
            String json = sB.toString();
            Gson gson = new Gson();
            return gson.fromJson(json, Network.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("File: " + filePath + " cannot be found.");
            return null;
        } catch (IOException e) {
            LOGGER.error("IOException during file reading.", e);
            return null;
        }
    }
}
