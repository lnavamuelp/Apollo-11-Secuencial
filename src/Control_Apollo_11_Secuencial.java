import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Control_Apollo_11_Secuencial {

    // Declaración de componentes de la interfaz gráfica
    private JPanel rootPanel;
    private JLabel lblTituloPral;
    private JLabel lblSegundosSal;
    private JButton btnInicio;
    private JButton btnCancelar;
    private JProgressBar barraProgreso;
    private JFormattedTextField txtSegundos;
    private JLabel lblsegundosentrada;
    private JLabel lblEstado;
    private volatile boolean detenerFase = false;


    private String txtMensaje = "Ha finalizado la cuenta atras. El transbordador ha sido lanzado correctamente.";
    private boolean hiloIniciado = false;


    // Declaración de variables relacionadas con la lógica de las fases de lanzamiento.
    private LaunchPhaseThread phase1Thread;
    private LaunchPhaseThread phase2Thread;
    private LaunchPhaseThread phase3Thread;
    private LaunchPhaseThread phase4Thread;

    //Método para detener la ejecución de las fases si se cancela por el usuario.
    private void detenerFases() {
        if (phase1Thread != null && !phase1Thread.isInterrupted()) {
            phase1Thread.interrupt();
        }
        if (phase2Thread != null && !phase2Thread.isInterrupted()) {
            phase2Thread.interrupt();
        }
        if (phase3Thread != null && !phase3Thread.isInterrupted()) {
            phase3Thread.interrupt();
        }
        if (phase4Thread != null && !phase4Thread.isInterrupted()) {
            phase4Thread.interrupt();
        }

        if (hiloIniciado) {
            MensajePersonalizado("La cuenta atrás ha sido cancelada. Lanzamiento transbordador abortado.");
            hiloIniciado = false;
        } else {
            MensajePersonalizado("La cuenta atrás no ha sido iniciada. Lanzamiento transbordador no iniciado.");
        }

        // Reiniciar variables antes de mostrar el mensaje de cancelación
        inicializarObjetos();
    }


    // Método para validar que los segundos introducidos por el usuario sean mayores que cero y sean un número entero.
    private boolean validarEntradaSegundos(String input) {
        // Expresión regular para un número entero positivo mayor que cero
        String regex = "^[1-9]\\d*$";

        if (input.matches(regex)) {
            try {
                int segundos = Integer.parseInt(input);
                if (segundos > 60) {
                    MensajePersonalizado("El número de segundos no debe ser mayor que 60.");
                    inicializarObjetos();
                }
                if (segundos > 0 && segundos <60) {
                    return true;  // La entrada es válida
                } else {
                    MensajePersonalizado("El número de segundos debe ser mayor que cero.");
                }
            } catch (NumberFormatException e) {
                MensajePersonalizado("Debes introducir un número entero de segundos válido y mayor que cero.");
            }
        } else {
            MensajePersonalizado("Debes introducir un número entero de segundos válido y mayor que cero.");
        }

        return false;  // La entrada no es válida
    }

    // Método para mostrar mensajes de error personalizados.
    private void MensajePersonalizado(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje);
    }

    // Método para reiniciar los componentes al estado inicial
    private void inicializarObjetos() {
        barraProgreso.setValue(0);
        barraProgreso.setString("");
        txtSegundos.setText("");
        lblSegundosSal.setText("");
        hiloIniciado = false;

//        // Detener todos los hilos antes de reiniciar
//        detenerFases();

        // Reiniciar variables relacionadas con las fases
        phase1Thread = null;
        phase2Thread = null;
        phase3Thread = null;
        phase4Thread = null;
//
//        // Mostrar el mensaje solo si la fase no fue cancelada
//        if (!detenerFase) {
//            txtMensaje = "Ha finalizado la cuenta atrás. El transbordador ha sido lanzado correctamente.";
//        }

        // Reiniciar el mensaje siempre
        txtMensaje = "Ha finalizado la cuenta atrás. El transbordador ha sido lanzado correctamente.";
    }

    public Control_Apollo_11_Secuencial() {

// Configuración de acciones para los botones de Iniciar y Detener Cuenta Atras.
        btnInicio.addActionListener(e -> {
            if (!hiloIniciado) {
                try {
                    String inputText = txtSegundos.getText();

                    // Validar la entrada de segundos y si es correcta lanzamos la cuenta atrás de fases.
                    if (validarEntradaSegundos(inputText)) {
                        int segundosFase1 = Integer.parseInt(inputText);
                        int ultimaFase = 4;  // Modifica según el número total de fases

                        // Si el hilo ya ha terminado o es nulo, crea uno nuevo.
                        if (phase1Thread == null || !phase1Thread.isAlive()) {
                            phase1Thread = new LaunchPhaseThread("Fase 1 - Preparación", barraProgreso, lblSegundosSal, segundosFase1, 1, ultimaFase);
                            phase2Thread = new LaunchPhaseThread("Fase 2 - Encendido de Motores", barraProgreso, lblSegundosSal, segundosFase1, 2, ultimaFase);
                            phase3Thread = new LaunchPhaseThread("Fase 3 - Despegue", barraProgreso, lblSegundosSal, segundosFase1, 3, ultimaFase);
                            phase4Thread = new LaunchPhaseThread("Fase 4 - Órbita", barraProgreso, lblSegundosSal, segundosFase1, 4, ultimaFase);

                            phase1Thread.setNextPhaseThread(phase2Thread);
                            phase2Thread.setNextPhaseThread(phase3Thread);
                            phase3Thread.setNextPhaseThread(phase4Thread);

                            // Inicia el primer hilo (Fase 1)
                            phase1Thread.start();
                            hiloIniciado = true;
                        } else {
                            // Si el hilo ya está en ejecución, reinicia la fase 1.
                            detenerFase = true;
                            inicializarObjetos();
                        }
                    } else {
                        inicializarObjetos();
                    }
                } catch (IllegalArgumentException ex) {
                    MensajePersonalizado(ex.getMessage());
                    inicializarObjetos();
                }
            } else {
                MensajePersonalizado("Ya hay una cuenta atrás de lanzamiento en ejecución. Debes esperar a que finalice o cancelarla antes de iniciar otra.");
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (hiloIniciado) {
                    detenerFase = true;
                    detenerFases();
                } else {
                    MensajePersonalizado("La cuenta atrás no ha sido iniciada.Lanzamiento transbordador no iniciado.");
                }
                inicializarObjetos();
            }
        });
    }

    public static void main(String[] args) {

        // Creación de la ventana principal
        JFrame frame = new JFrame("Control Misión Apollo 11");

        // Creación del panel principal que contendrá todos los componentes
        frame.setContentPane(new Control_Apollo_11_Secuencial().rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    // Clase que representa el hilo principal de fases.
    private class LaunchPhaseThread extends Thread {
        private final String phaseName;
        private final JProgressBar progressBar;
        private final JLabel countdownLabel;
        private final int phaseSeconds;
        public boolean detenerFase;
        private LaunchPhaseThread nextPhaseThread;


        private final int faseActual;
        private final int ultimaFase;

        public LaunchPhaseThread(String phaseName, JProgressBar progressBar, JLabel countdownLabel, int phaseSeconds,int faseActual, int ultimaFase) {
            this.phaseName = phaseName;
            this.progressBar = progressBar;
            this.countdownLabel = countdownLabel;
            this.phaseSeconds = phaseSeconds;
            this.faseActual = faseActual;
            this.ultimaFase = ultimaFase;
            progressBar.setMaximum(phaseSeconds);
            progressBar.setString(phaseName);

        }

        public void setNextPhaseThread(LaunchPhaseThread nextPhaseThread) {
            this.nextPhaseThread = nextPhaseThread;
        }

        @Override
        public void run() {

            SwingUtilities.invokeLater(() -> {
                // Actualiza el nombre de la fase en la progress bar.
                progressBar.setString(phaseName);
            });

            for (int i = 1; i <= phaseSeconds && !detenerFase; i++) {
                final int segundosRestantes = phaseSeconds - i;
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(segundosRestantes);
                    //countdownLabel.setText(Integer.toString(phaseSeconds - segundosRestantes));
                    countdownLabel.setText(Integer.toString(segundosRestantes));
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (nextPhaseThread != null) {
                nextPhaseThread.start();
                try {
                    nextPhaseThread.join();  // Espera a que la siguiente fase termine antes de continuar
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }

            // Verifica si es la última fase y muestra el mensaje de éxito siempre que no se haya cancelado el lanzamiento.
            if (!detenerFase) {
                // Verifica si es la última fase y muestra el mensaje de éxito
                if (faseActual == ultimaFase)
                {
                    SwingUtilities.invokeLater(() -> {
                        MensajePersonalizado("Ha finalizado la cuenta atrás. El transbordador ha sido lanzado correctamente.");
                    });
                }
            }
            detenerFase=true;

            SwingUtilities.invokeLater(() -> {
                inicializarObjetos();
            });

        }

    }
}
