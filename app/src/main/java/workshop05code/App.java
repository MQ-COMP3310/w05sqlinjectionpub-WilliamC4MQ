package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
//Included for the logging exercise
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import java.util.regex.Pattern; // for query sanitisation

/**
 *
 * @author sqlitetutorial.net
 */
public class App {
    // Start code for logging exercise
    static {
        // must set before the Logger
        // loads logging.properties from the classpath
        try {// resources\logging.properties
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());
    // End code for logging exercise
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("Wordle created and connected.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            System.out.println("Wordle structures in place.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            return;
        }

        // let's add some words to valid 4 letter words from the data.txt file

        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.length() != 4) { // Assuming valid words must be 4 letters
                    logger.log(Level.SEVERE, "Invalid word in data.txt: " + line);
                } else {
                    logger.log(Level.INFO, "Valid word added from data.txt: " + line);
                    wordleDatabaseConnection.addValidWord(i, line);
                    i++;
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load data.txt.", e);
            return;
        }

        // let's get them to enter a word

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4-letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();
        
            while (!guess.equals("q")) {
                if (guess.length() != 4) {
                    logger.log(Level.WARNING, "Invalid guess: " + guess);
                    System.out.println("Invalid guess. Please enter a 4-letter word.");
                } else if (wordleDatabaseConnection.isValidWord(guess)) {
                    System.out.println("Success! It is in the list.");
                } else if (!Pattern.matches("^[a-zA-Z0-9\\s]+$", guess)) { // if query contains anything other than letters, numbers and spaces
                        logger.log(Level.INFO, "Illegal characters guessed: " + guess);
                        System.out.println("Invalid input, guess using only letters.");
                } else {
                    logger.log(Level.INFO, "Invalid word guessed: " + guess);
                    System.out.println("Sorry. This word is NOT in the list.");
                }
        
                System.out.print("Enter a 4-letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Error during user input.", e);
        }

    }
}