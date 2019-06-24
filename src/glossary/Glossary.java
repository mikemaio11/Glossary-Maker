package glossary;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Glossary extends Application{
	static String subscriptionKey;
    static String host = "https://api.cognitive.microsofttranslator.com";
    //static String path = "/translate?api-version=3.0";
    static String path = "/dictionary/lookup?api-version=3.0";
    //static String params = "&to=en";
    static String params = "&from=fr&to=en";
    static String text = "";
    static File file;
    
    static File inputFile;
    static File outputFilePath;
    static String outputFileNames;
    static int lengthOfWords;
    static int numberOfWords;
    
    static Stage primaryStage;

    public static class RequestBody {
        String Text;
        public RequestBody(String text) {
            this.Text = text;
        }
    }
	
    public static String Post (URL url, String content) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", content.length() + "");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setRequestProperty("X-ClientTraceId", java.util.UUID.randomUUID().toString());
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        byte[] encoded_content = content.getBytes("UTF-8");
        wr.write(encoded_content, 0, encoded_content.length);
        wr.flush();
        wr.close();

        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }
    
    public static String Translate () throws Exception {
        URL url = new URL (host + path + params);

        text = "formuler";
        List<RequestBody> objList = new ArrayList<RequestBody>();
        objList.add(new RequestBody(text));
        String content = new Gson().toJson(objList);
        
        System.out.println(content);

        return Post(url, content);
    }
    
    public static String prettify(String json_text){
    	JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        JsonArray arr = json.getAsJsonArray();
        JsonObject obj = arr.get(0).getAsJsonObject();
		
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(obj.get("translations").getAsJsonArray().get(0).getAsJsonObject().get("text"));
    }
    
    public static HashMap<String, Integer> getTokens() throws InvalidPasswordException, IOException{
    	PDDocument document = PDDocument.load(inputFile);
    	String text = new PDFTextStripper().getText(document);
    	HashMap<String, Integer> tokens = new HashMap<String, Integer>();
    	
    	String[] textArray = text.split("\\s+");
    	for(int i=0;i<textArray.length;i++){
    		String temp = textArray[i];
    		temp = temp.replaceFirst("^[^a-zA-Z]+", "");
    		temp = temp.replaceAll("[^a-zA-Z]+$", "");
    		temp = temp.replaceAll("\\d+", "");
    		temp = temp.toLowerCase();
    		
    		if(temp.length() >= lengthOfWords){
    			if(!tokens.containsKey(temp))
    				tokens.put(temp, 1);
    			else
    				tokens.put(temp, tokens.get(temp)+1);
    		}
    	}
    	document.close();
		return tokens;
    }
    
    public static List<Entry<String, Integer>> sortMap(HashMap<String, Integer> map){
    	Set<Entry<String, Integer>> set = map.entrySet();
    	List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
    	 Collections.sort( list, new Comparator<Map.Entry<String, Integer>>(){
    		 public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ){
                 return (o1.getValue()).compareTo(o2.getValue());
             }
    	 });
    	 return list;
    }
    
    public static void newPage(String[] input, String[] output, int start, PDDocument document) throws IOException{
    	PDPage page = new PDPage();
		document.addPage(page);
		PDFont font = PDType1Font.HELVETICA_BOLD;
		PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
		
		int offset = 725;
		int total = 0;
		while(offset>60 && total<input.length){
			if(total % 2 == 0){
				contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
				contentStream.addRect(50, offset-3, 500, 13);
				contentStream.fill();
			}
			total++;
			offset -= 15;
		}
		
		contentStream.setNonStrokingColor(Color.BLACK);
		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.newLineAtOffset(150, 725);
		offset = 725;
		
		for(int j=start;j<input.length;j++){
			int text_width = (int)((font.getStringWidth(input[j])/1000.0f)*12);
			contentStream.newLineAtOffset(-text_width, 0);
			contentStream.showText(input[j]);
			contentStream.newLineAtOffset(text_width, -15);
			offset -= 15;
			if(offset<60){
				offset = 725;
				if(j==input.length-1){
					contentStream.endText();
					contentStream.close();
					return;
				}
				newPage(input, output, j+1, document);
				break;
			}
		}
		contentStream.endText();
		contentStream.close();
    }
    
    public static void newPageOutput(String[] output, int start, File file, int pageNum) throws InvalidPasswordException, IOException{
    	PDDocument document = PDDocument.load(file);
    	PDFont font = PDType1Font.HELVETICA;
    	int count = 0;
    	for(int i=pageNum;i<document.getNumberOfPages();i++){
    		if(count>=output.length)
    			return;
    		PDPage page = document.getPage(i);
    		PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
    		contentStream.beginText();
    		contentStream.setFont(font, 12);
    		contentStream.newLineAtOffset(210, 725);
    		int offset = 725;
    		while(offset>60 && count<output.length){
    			contentStream.showText(output[count]);
    			count++;
    			contentStream.newLineAtOffset(0, -15);
    			offset -= 15;
    		}
    		contentStream.endText();
    		contentStream.moveTo(180, 735);
    		contentStream.lineTo(180, 60);
    		contentStream.stroke();
    		contentStream.close();
    	}
    	document.save(file);
    	document.close();
    }
    
    public static void translate() throws InvalidPasswordException, IOException{
    	HashMap<String, Integer> words = getTokens();
		List<Entry<String, Integer>> freqWords = sortMap(words);
		int translateNum = numberOfWords;
		int translateCalls;
		if(translateNum <135)
			translateCalls = 1;
		else
			translateCalls = translateNum/135+1;
		int total = 0;
		
		String path = outputFilePath.toString();
		path = path.replaceAll("\\\\", "//");
		path += "//"+outputFileNames;
		
		PDDocument document = new PDDocument();
		document.save(new File(path+".pdf"));
		document.close();

		try {
			for(int i=0;i<translateCalls;i++){
				text = "";
				for(int j=0;j<135;j++){
					total++;
					if(total >= freqWords.size() || total>=translateNum)
						break;
					text += freqWords.get(0).getKey() + " :: ";
					freqWords.remove(0);
				}

				String translations = prettify(Translate());
				
				String[] inputArray = text.split(" :: ");
				String[] outputArray = translations.substring(1, translations.length()-1).split(":: ");
				
				PDDocument doc = PDDocument.load(new File(path+".pdf"));
				
				newPage(inputArray, outputArray, 0, doc);
				doc.save(new File(path+".pdf"));
				doc.close();
				newPageOutput(outputArray, 0, new File(path+".pdf"), i*3);
			}
			primaryStage.close();
        }
        catch (Exception e) {
            System.out.println (e);
        }
    }
    
    @Override
	public void start(Stage primaryStages) throws Exception {
    	primaryStage = primaryStages;
    	primaryStage.getIcons().add(new Image("file:book.png"));
    	GridPane root = new GridPane();
		root.setHgap(5); root.setVgap(5);
		
		TextField inFile = new TextField();
		inFile.setMinWidth(330);
		inFile.setDisable(true);
		Button input = new Button();
		input.setText("Input PDF");
		input.setMinWidth(105);
		input.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Input PDF");
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
				fileChooser.getExtensionFilters().add(extFilter);
				inputFile = fileChooser.showOpenDialog(primaryStage);
				inFile.setText(inputFile.toString());
			}
		});
		
		TextField outFolder = new TextField();
		outFolder.setDisable(true);
		Button output = new Button();
		output.setText("Output Location");
		output.setMinWidth(105);
		output.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				DirectoryChooser dirChooser = new DirectoryChooser();
				dirChooser.setTitle("Output Location");
				outputFilePath = dirChooser.showDialog(primaryStage);
				outFolder.setText(outputFilePath.toString());
			}
		});
		
		Text api = new Text("API Key: ");
		api.setFont(Font.font("Arial", 14));
		TextField apiKey = new TextField();
		
		Text outFileName = new Text("Output file name: ");
		outFileName.setFont(Font.font("Arial", 14));
		TextField outputFileName = new TextField();
		
		Text wordLen = new Text("Word length >= : ");
		wordLen.setFont(Font.font("Arial", 14));
		TextField wordLength = new TextField();
		wordLength.setMaxWidth(30);
		wordLength.setAlignment(Pos.BASELINE_RIGHT);
		wordLength.textProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				if (!newValue.matches("\\d*")) {
		            wordLength.setText(newValue.replaceAll("[^\\d]", ""));
		        }
				if(newValue.length()>2){
					wordLength.setText(newValue.substring(0, 2));
				}
			}
		});
		
		Text numWords = new Text("Words to translate: ");
		numWords.setFont(Font.font("Arial", 14));
		TextField numberWords = new TextField();
		numberWords.setMaxWidth(50);
		numberWords.setAlignment(Pos.BASELINE_RIGHT);
		numberWords.textProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
		            numberWords.setText(newValue.replaceAll("[^\\d]", ""));
		        }
				if(newValue.length()>5){
					numberWords.setText(newValue.substring(0, 5));
				}
			}
		});
		
		Button generate = new Button();
		generate.setText("Generate Glossary");
		generate.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				try {
					numberOfWords = Integer.parseInt(numberWords.getText())+1;
					lengthOfWords = Integer.parseInt(wordLength.getText());
					outputFileNames = outputFileName.getText();
					subscriptionKey = apiKey.getText();
					
					PrintWriter writer = new PrintWriter(new FileWriter("config.txt"));
					writer.write(inFile.getText()+"\n");
					writer.write(outFolder.getText()+"\n");
					writer.write(apiKey.getText()+"\n");
					writer.write(wordLength.getText()+"\n");
					writer.write(numberWords.getText()+"\n");
					writer.close();
					translate();
				} catch (InvalidPasswordException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		FileReader fileReader = new FileReader("config.txt");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		int lineCount = 1;
		
		while((line = bufferedReader.readLine()) != null){
			if(lineCount == 1)
				inFile.setText(line);
			else if(lineCount == 2)
				outFolder.setText(line);
			else if(lineCount == 3)
				apiKey.setText(line);
			else if(lineCount == 4)
				wordLength.setText(line);
			else if(lineCount == 5)
				numberWords.setText(line);
			lineCount++;
		}
		fileReader.close(); bufferedReader.close();
		
		root.add(input, 2, 2); root.add(inFile, 5, 2);
		root.add(output, 2, 4); root.add(outFolder, 5, 4);
		root.add(outFileName, 2, 7); root.add(outputFileName, 5, 7);
		root.add(api, 2, 9); root.add(apiKey, 5, 9);
		root.add(wordLen, 2, 12); root.add(wordLength, 5, 12);
		root.add(numWords, 2, 15); root.add(numberWords, 5, 15);
		root.add(generate, 5, 18);
		Scene scene = new Scene(root, 500, 300);
		
		primaryStage.setTitle("Glossary Maker");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
    
	public static void main(String[] args) throws InvalidPasswordException, IOException{
		launch(args);
	}
}