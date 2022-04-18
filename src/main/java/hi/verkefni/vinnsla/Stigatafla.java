package hi.verkefni.vinnsla;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.tinylog.Logger;

public class Stigatafla {
    private static final String STIGATAFLAURL = "stigatafla.txt";

    private Stig[] stigArray;
    private Stig currentPlayer1;
    private Stig currentPlayer2;

    public Stigatafla() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResource(STIGATAFLAURL).openStream()))) {
            ArrayList<Stig> stigAL = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split("\\s+");
                String nafn = splitLine[0];
                int score = Integer.parseInt(splitLine[1]);
                stigAL.add(new Stig(nafn, score));
            }
            stigArray = new Stig[stigAL.size()];
            stigArray = stigAL.toArray(stigArray);
        } catch (FileNotFoundException e) {
            Logger.error("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Logger.error("Error while reading file");
            e.printStackTrace();
        }
    }

    public void add(Stig stigP1) {
        if (currentPlayer1 == null) {
            currentPlayer1 = stigP1;
            return;
        }
        if (stigP1.compareTo(currentPlayer1) > 0) {
            currentPlayer1 = stigP1;
        }
    }

    public void add(Stig stigP1, Stig stigP2) {
        if (currentPlayer1 == null) {
            currentPlayer1 = stigP1;
        }
        if (currentPlayer2 == null) {
            currentPlayer2 = stigP2;
        }
        if (stigP1.compareTo(currentPlayer1) > 0) {
            currentPlayer1 = stigP1;
        }
        if (stigP2.compareTo(currentPlayer2) > 0) {
            currentPlayer2 = stigP2;
        }
    }

    public void writeToFile() {
        if (currentPlayer1 == null) {
            return;
        }
        if (currentPlayer2 == null) {
            int i = 0;
            boolean found = false;
            for (Stig stig : stigArray) {
                if (currentPlayer1.getNafn().toLowerCase().equals(stig.getNafn().toLowerCase())) {
                    if (currentPlayer1.compareTo(stig) > 0) {
                        stigArray[i] = currentPlayer1;
                    }
                    found = true;
                }
                i++;
            }
            if (!found) {
                Stig[] tempStigArray = new Stig[stigArray.length + 1];
                System.arraycopy(stigArray, 0, tempStigArray, 0, stigArray.length);
                tempStigArray[stigArray.length] = currentPlayer1;
                stigArray = tempStigArray;
            }
        } else {
            int i = 0;
            boolean[] found = new boolean[] { false, false };
            for (Stig stig : stigArray) {
                if (currentPlayer1.getNafn().toLowerCase().equals(stig.getNafn().toLowerCase())) {
                    if (currentPlayer1.compareTo(stig) > 0) {
                        stigArray[i] = currentPlayer1;
                    }
                    found[0] = true;
                }
                if (currentPlayer2.getNafn().toLowerCase().equals(stig.getNafn().toLowerCase())) {
                    if (currentPlayer2.compareTo(stig) > 0) {
                        stigArray[i] = currentPlayer2;
                    }
                    found[1] = true;
                }
                i++;
            }
            if (!found[0]) {
                Stig[] tempStigArray = new Stig[stigArray.length + 1];
                System.arraycopy(stigArray, 0, tempStigArray, 0, stigArray.length);
                tempStigArray[stigArray.length] = currentPlayer1;
                stigArray = tempStigArray;
            }
            if (!found[1]) {
                Stig[] tempStigArray = new Stig[stigArray.length + 1];
                System.arraycopy(stigArray, 0, tempStigArray, 0, stigArray.length);
                tempStigArray[stigArray.length] = currentPlayer2;
                stigArray = tempStigArray;
            }
        }
        Arrays.sort(stigArray);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STIGATAFLAURL));) {
            for (int i = stigArray.length - 1; i >= 0; i--) {
                writer.write(stigArray[i].toString());
                writer.newLine();
            }
        } catch (FileNotFoundException e) {
            Logger.error("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Logger.error("Error while writing to file");
            e.printStackTrace();
        }
    }

    public Stig[] getStigArray() {
        return stigArray;
    }

    public static void main(String[] args) {
        Stigatafla test1 = new Stigatafla();
        for (Stig stig : test1.stigArray) {
            Logger.info(stig);
        }
        test1.add(new Stig("Sara", 1300), new Stig("Sophie", 3000));
        test1.writeToFile();
    }
}
