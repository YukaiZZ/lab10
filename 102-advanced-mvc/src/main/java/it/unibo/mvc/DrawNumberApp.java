package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.io.FileReader;
import java.io.File;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final File configfile, final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
       
        final Configuration.Builder configbuilder = new Configuration.Builder();
        try (BufferedReader br = new BufferedReader(new FileReader(configfile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("min")) {
                String []parts = line.split(": ");
                     int min = Integer.parseInt(parts[1]); 
                     configbuilder.setMin(min);            
                   
                }
                else if (line.contains("max")) {
                    String []parts = line.split(": ");
                    for (String part : parts) {
                     int max = Integer.parseInt(parts[1]); 
                     configbuilder.setMax(max);        
                   }
                }
                else if (line.contains("attempts")) {
                    String []parts = line.split(": ");
                    for (String part : parts) {
                     int attempts = Integer.parseInt(parts[1]); 
                     configbuilder.setAttempts(attempts);        
                   }
                }
            }
        } catch (IOException | NumberFormatException e) {
            for (DrawNumberView drawNumberView : views) {
                drawNumberView.displayError(e.getMessage());
            }
        }
        final Configuration c = configbuilder.build();
        if (c.isConsistent()) {
            this.model = new DrawNumberImpl(c.getMin(), c.getMax(), c.getAttempts());
        } else {
            System.out.println("C ERROR");
            this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new File("src/main/resources/config.yml"), 
        new DrawNumberViewImpl(),
        new DrawNumberViewImpl(),
        new PrintStreamView(System.out),
        new PrintStreamView("output.log"));
    }

}
