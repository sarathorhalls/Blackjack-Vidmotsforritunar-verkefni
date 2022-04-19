package hi.verkefni.vinnsla;

/******************************************************************************
 *  Nafn    : Sara Þórhallsdóttir
 *  T-póstur: kgt2@hi.is
 *
 *  Lýsing  : les inn stigatöflu og heldur utan um hana, skrifar síðan aftur
 *  í stigatöfluna þegar leikmaður lýkur leik
 *****************************************************************************/

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
    // static final Tilviksbreytur
    private static final String STIGATAFLAURL = "stigatafla.txt";

    // Java Tilviksbreytur
    private Stig[] stigArray;
    private Stig currentPlayer1;
    private Stig currentPlayer2;

    /**
     * Constructor
     */
    public Stigatafla() {
        // Les inn stigatöfluna úr skjali
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

    /**
     * Bætir score frá leikmanni í geymslu sem skrifað er síðan í skjal þegar leik
     * er lokað
     * 
     * @param stigP1 Stig frá leikmanni 1
     */
    public void add(Stig stigP1) {
        if (currentPlayer1 == null) {
            currentPlayer1 = stigP1;
            return;
        }
        if (stigP1.compareTo(currentPlayer1) > 0) {
            currentPlayer1 = stigP1;
        }
    }

    /**
     * Bætir score frá leikmönnum í geymslu sem skrifað er síðan í skjal þegar leik
     * er lokað
     * 
     * @param stigP1 Stig frá leikmanni 1
     * @param stigP2 Stig frá leikmanni 2
     */
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

    /**
     * Skrifar aftur í stigatöflu skjalið eftir að leikmaður klárar leik
     */
    public void writeToFile() {
        // Ef leikmaður 1 hefur ekki breytt score gerum við ekkert
        if (currentPlayer1 == null) {
            return;
        }
        // Ef leikmaður 2 hefur ekki breytt score geymum við bara Stig frá leikmanni 1
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
            // Ef báðir leikmenn hafa breytt score geymum við frá þeim báðum
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
        // Setjum stiginn í röð
        Arrays.sort(stigArray);

        // Þar sem röðin er öfug byrjum við frá botninum og skrifum í stigatafla skjal
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

    // Getters and Setters

    public Stig[] getStigArray() {
        return stigArray;
    }

    /**
     * Test function fyrir Stig
     * 
     * @param args
     */
    public static void main(String[] args) {
        Stigatafla test1 = new Stigatafla();
        for (Stig stig : test1.stigArray) {
            Logger.info(stig);
        }
        test1.add(new Stig("Sara", 1300), new Stig("Sophie", 3000));
        test1.writeToFile();
    }
}
